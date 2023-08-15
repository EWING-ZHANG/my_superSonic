package com.tencent.supersonic.semantic.query.parser.calcite.sql.render;

import com.tencent.supersonic.semantic.api.query.request.MetricReq;
import com.tencent.supersonic.semantic.query.parser.calcite.dsl.Constants;
import com.tencent.supersonic.semantic.query.parser.calcite.dsl.DataSource;
import com.tencent.supersonic.semantic.query.parser.calcite.dsl.Dimension;
import com.tencent.supersonic.semantic.query.parser.calcite.dsl.Identify;
import com.tencent.supersonic.semantic.query.parser.calcite.dsl.Metric;
import com.tencent.supersonic.semantic.query.parser.calcite.schema.SemanticSchema;
import com.tencent.supersonic.semantic.query.parser.calcite.sql.Renderer;
import com.tencent.supersonic.semantic.query.parser.calcite.sql.TableView;
import com.tencent.supersonic.semantic.query.parser.calcite.sql.node.AggFunctionNode;
import com.tencent.supersonic.semantic.query.parser.calcite.sql.node.DataSourceNode;
import com.tencent.supersonic.semantic.query.parser.calcite.sql.node.FilterNode;
import com.tencent.supersonic.semantic.query.parser.calcite.sql.node.MetricNode;
import com.tencent.supersonic.semantic.query.parser.calcite.sql.node.SemanticNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.JoinConditionType;
import org.apache.calcite.sql.JoinType;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlJoin;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.validate.SqlValidatorScope;

@Slf4j
public class JoinRender extends Renderer {

    @Override
    public void render(MetricReq metricCommand, List<DataSource> dataSources, SqlValidatorScope scope,
            SemanticSchema schema, boolean nonAgg) throws Exception {
        String queryWhere = metricCommand.getWhere();
        Set<String> whereFields = new HashSet<>();
        List<String> fieldWhere = new ArrayList<>();
        if (queryWhere != null && !queryWhere.isEmpty()) {
            SqlNode sqlNode = SemanticNode.parse(queryWhere, scope);
            FilterNode.getFilterField(sqlNode, whereFields);
            fieldWhere = whereFields.stream().collect(Collectors.toList());
        }
        Set<String> queryAllDimension = new HashSet<>();
        List<String> measures = new ArrayList<>();
        DataSourceNode.getQueryDimensionMeasure(schema, metricCommand, queryAllDimension, measures);
        SqlNode left = null;
        TableView leftTable = null;
        TableView innerView = new TableView();
        TableView filterView = new TableView();
        Map<String, SqlNode> innerSelect = new HashMap<>();
        Set<String> filterDimension = new HashSet<>();

        for (int i = 0; i < dataSources.size(); i++) {
            final DataSource dataSource = dataSources.get(i);
            final Set<String> filterDimensions = new HashSet<>();
            final Set<String> filterMetrics = new HashSet<>();
            final List<String> queryDimension = new ArrayList<>();
            final List<String> queryMetrics = new ArrayList<>();
            SourceRender.whereDimMetric(fieldWhere, queryMetrics, queryDimension, dataSource, schema, filterDimensions,
                    filterMetrics);
            List<String> reqMetric = new ArrayList<>(metricCommand.getMetrics());
            reqMetric.addAll(filterMetrics);
            reqMetric = uniqList(reqMetric);

            List<String> reqDimension = new ArrayList<>(metricCommand.getDimensions());
            reqDimension.addAll(filterDimensions);
            reqDimension = uniqList(reqDimension);

            Set<String> sourceMeasure = dataSource.getMeasures().stream().map(mm -> mm.getName())
                    .collect(Collectors.toSet());
            doMetric(innerSelect, filterView, queryMetrics, reqMetric, dataSource, sourceMeasure, scope, schema,
                    nonAgg);
            Set<String> dimension = dataSource.getDimensions().stream().map(dd -> dd.getName())
                    .collect(Collectors.toSet());
            doDimension(innerSelect, filterDimension, queryDimension, reqDimension, dataSource, dimension, scope,
                    schema);
            List<String> primary = new ArrayList<>();
            for (Identify identify : dataSource.getIdentifiers()) {
                primary.add(identify.getName());
                if (!fieldWhere.contains(identify.getName())) {
                    fieldWhere.add(identify.getName());
                }
            }
            TableView tableView = SourceRender.renderOne("", fieldWhere, queryMetrics, queryDimension,
                    metricCommand.getWhere(), dataSources.get(i), scope, schema, true);
            log.info("tableView {}", tableView.getTable().toString());
            String alias = Constants.JOIN_TABLE_PREFIX + dataSource.getName();
            tableView.setAlias(alias);
            tableView.setPrimary(primary);
            if (left == null) {
                leftTable = tableView;
                left = SemanticNode.buildAs(tableView.getAlias(), getTable(tableView, scope));
                continue;
            }

            left = new SqlJoin(
                    SqlParserPos.ZERO,
                    left,
                    SqlLiteral.createBoolean(false, SqlParserPos.ZERO),
                    SqlLiteral.createSymbol(JoinType.INNER, SqlParserPos.ZERO),
                    SemanticNode.buildAs(tableView.getAlias(), getTable(tableView, scope)),
                    SqlLiteral.createSymbol(JoinConditionType.ON, SqlParserPos.ZERO),
                    getCondition(leftTable, tableView, dataSource, schema, scope));
            leftTable = tableView;
        }

        for (Map.Entry<String, SqlNode> entry : innerSelect.entrySet()) {
            innerView.getMeasure().add(entry.getValue());
        }
        innerView.setTable(left);
        filterView.setTable(SemanticNode.buildAs(Constants.JOIN_TABLE_OUT_PREFIX, innerView.build()));
        if (!filterDimension.isEmpty()) {
            for (String d : filterDimension) {
                if (nonAgg) {
                    filterView.getMeasure().add(SemanticNode.parse(d, scope));
                } else {
                    filterView.getDimension().add(SemanticNode.parse(d, scope));
                }

            }
        }
        super.tableView = filterView;
    }

    private void doMetric(Map<String, SqlNode> innerSelect, TableView filterView, List<String> queryMetrics,
            List<String> reqMetrics, DataSource dataSource, Set<String> sourceMeasure, SqlValidatorScope scope,
            SemanticSchema schema, boolean nonAgg) throws Exception {
        String alias = Constants.JOIN_TABLE_PREFIX + dataSource.getName();
        for (String m : reqMetrics) {
            if (getMatchMetric(schema, sourceMeasure, m, queryMetrics)) {
                MetricNode metricNode = buildMetricNode(m, dataSource, scope, schema, nonAgg, alias);

                if (!metricNode.getNonAggNode().isEmpty()) {
                    for (String measure : metricNode.getNonAggNode().keySet()) {
                        innerSelect.put(measure,
                                SemanticNode.buildAs(measure, SemanticNode.parse(alias + "." + measure, scope)));
                    }

                }
                if (metricNode.getAggFunction() != null && !metricNode.getAggFunction().isEmpty()) {
                    for (Map.Entry<String, String> entry : metricNode.getAggFunction().entrySet()) {
                        if (metricNode.getNonAggNode().containsKey(entry.getKey())) {
                            if (nonAgg) {
                                filterView.getMeasure().add(SemanticNode.buildAs(entry.getKey(),
                                        SemanticNode.parse(entry.getKey(), scope)));
                            } else {
                                filterView.getMeasure().add(SemanticNode.buildAs(entry.getKey(),
                                        AggFunctionNode.build(entry.getValue(), entry.getKey(), scope)));
                            }

                        }
                    }

                }
            }
        }
    }

    private void doDimension(Map<String, SqlNode> innerSelect, Set<String> filterDimension, List<String> queryDimension,
            List<String> reqDimensions, DataSource dataSource, Set<String> dimension, SqlValidatorScope scope,
            SemanticSchema schema) throws Exception {
        String alias = Constants.JOIN_TABLE_PREFIX + dataSource.getName();
        for (String d : reqDimensions) {
            if (getMatchDimension(schema, dimension, dataSource, d, queryDimension)) {
                if (d.contains(Constants.DIMENSION_IDENTIFY)) {
                    String[] identifyDimension = d.split(Constants.DIMENSION_IDENTIFY);
                    innerSelect.put(d,
                            SemanticNode.buildAs(d, SemanticNode.parse(alias + "." + identifyDimension[1], scope)));
                } else {
                    innerSelect.put(d, SemanticNode.buildAs(d, SemanticNode.parse(alias + "." + d, scope)));

                }
                filterDimension.add(d);
            }
        }
    }


    private boolean getMatchMetric(SemanticSchema schema, Set<String> sourceMeasure, String m,
            List<String> queryMetrics) {
        Optional<Metric> metric = schema.getMetrics().stream().filter(mm -> mm.getName().equalsIgnoreCase(m))
                .findFirst();
        boolean isAdd = false;
        if (metric.isPresent()) {
            Set<String> metricMeasures = metric.get().getMetricTypeParams().getMeasures().stream()
                    .map(me -> me.getName()).collect(Collectors.toSet());
            if (sourceMeasure.containsAll(metricMeasures)) {
                isAdd = true;
            }
        }
        if (sourceMeasure.contains(m)) {
            isAdd = true;
        }
        if (isAdd && !queryMetrics.contains(m)) {
            queryMetrics.add(m);
        }
        return isAdd;
    }

    private boolean getMatchDimension(SemanticSchema schema, Set<String> sourceDimension, DataSource dataSource,
            String d, List<String> queryDimension) {
        String oriDimension = d;
        boolean isAdd = false;
        if (d.contains(Constants.DIMENSION_IDENTIFY)) {
            oriDimension = d.split(Constants.DIMENSION_IDENTIFY)[1];
        }
        if (sourceDimension.contains(oriDimension)) {
            isAdd = true;
        }
        for (Identify identify : dataSource.getIdentifiers()) {
            if (identify.getName().equalsIgnoreCase(oriDimension)) {
                isAdd = true;
                break;
            }
        }
        if (schema.getDimension().containsKey(dataSource.getName())) {
            for (Dimension dim : schema.getDimension().get(dataSource.getName())) {
                if (dim.getName().equalsIgnoreCase(oriDimension)) {
                    isAdd = true;
                }
            }
        }
        if (isAdd && !queryDimension.contains(oriDimension)) {
            queryDimension.add(oriDimension);
        }
        return isAdd;
    }

    private SqlNode getTable(TableView tableView, SqlValidatorScope scope) throws Exception {
        return SemanticNode.getTable(tableView.getTable());
    }

    private SqlNode getCondition(TableView left, TableView right, DataSource dataSource, SemanticSchema schema,
            SqlValidatorScope scope) throws Exception {
        log.info(left.getClass().toString());
        Set<String> selectLeft = SemanticNode.getSelect(left.getTable());
        Set<String> selectRight = SemanticNode.getSelect(right.getTable());
        selectLeft.retainAll(selectRight);
        SqlNode condition = null;
        for (String on : selectLeft) {
            if (!SourceRender.isDimension(on, dataSource, schema)) {
                continue;
            }
            List<SqlNode> ons = new ArrayList<>();
            ons.add(SemanticNode.parse(left.getAlias() + "." + on, scope));
            ons.add(SemanticNode.parse(right.getAlias() + "." + on, scope));
            if (condition == null) {
                condition = new SqlBasicCall(
                        SqlStdOperatorTable.EQUALS,
                        ons,
                        SqlParserPos.ZERO, null);
                continue;
            }
            SqlNode addCondition = new SqlBasicCall(
                    SqlStdOperatorTable.EQUALS,
                    ons,
                    SqlParserPos.ZERO, null);
            condition = new SqlBasicCall(
                    SqlStdOperatorTable.AND,
                    new ArrayList<>(Arrays.asList(condition, addCondition)),
                    SqlParserPos.ZERO, null);
        }
        return condition;
    }
}
