package com.tencent.supersonic.semantic.query.domain.parser.convertor.planner;


import com.tencent.supersonic.semantic.api.query.request.MetricReq;
import com.tencent.supersonic.semantic.query.domain.parser.convertor.sql.Renderer;
import com.tencent.supersonic.semantic.query.domain.parser.convertor.sql.TableView;
import com.tencent.supersonic.semantic.query.domain.parser.convertor.sql.node.DataSourceNode;
import com.tencent.supersonic.semantic.query.domain.parser.convertor.sql.node.SemanticNode;
import com.tencent.supersonic.semantic.query.domain.parser.convertor.sql.render.FilterRender;
import com.tencent.supersonic.semantic.query.domain.parser.convertor.sql.render.OutputRender;
import com.tencent.supersonic.semantic.query.domain.parser.convertor.sql.render.SourceRender;
import com.tencent.supersonic.semantic.query.domain.parser.dsl.DataSource;
import com.tencent.supersonic.semantic.query.domain.parser.schema.SchemaBuilder;
import com.tencent.supersonic.semantic.query.domain.parser.schema.SemanticSchema;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.validate.SqlValidatorScope;

public class AggPlanner implements Planner {

    private MetricReq metricCommand;
    private SemanticSchema schema;
    private SqlValidatorScope scope;
    private Stack<TableView> dataSets = new Stack<>();
    private SqlNode parserNode;
    private String sourceId;
    private boolean isAgg = true;

    public AggPlanner(SemanticSchema schema) {
        this.schema = schema;
    }


    public void parse() throws Exception {
        // find the match Datasource
        scope = SchemaBuilder.getScope(schema);
        List<DataSource> datasource = getMatchDataSource(scope);
        if (datasource == null || datasource.isEmpty()) {
            throw new Exception("datasource not found");
        }
        sourceId = String.valueOf(datasource.get(0).getSourceId());

        // build  level by level
        LinkedList<Renderer> builders = new LinkedList<>();
        builders.add(new SourceRender());
        builders.add(new FilterRender());
        builders.add(new OutputRender());
        ListIterator<Renderer> it = builders.listIterator();
        int i = 0;
        Renderer previous = null;
        while (it.hasNext()) {
            Renderer renderer = it.next();
            if (previous != null) {
                previous.render(metricCommand, datasource, scope, schema, !isAgg);
                renderer.setTable(previous.builderAs(DataSourceNode.getNames(datasource) + "_" + String.valueOf(i)));
                i++;
            }
            previous = renderer;
        }
        builders.getLast().render(metricCommand, datasource, scope, schema, !isAgg);
        parserNode = builders.getLast().builder();


    }


    private List<DataSource> getMatchDataSource(SqlValidatorScope scope) throws Exception {
        return DataSourceNode.getMatchDataSources(scope, schema, metricCommand);
    }


    @Override
    public void explain(MetricReq metricCommand, boolean isAgg) throws Exception {
        this.metricCommand = metricCommand;
        if (metricCommand.getMetrics() == null) {
            metricCommand.setMetrics(new ArrayList<>());
        }
        if (metricCommand.getDimensions() == null) {
            metricCommand.setDimensions(new ArrayList<>());
        }
        if (metricCommand.getLimit() == null) {
            metricCommand.setLimit(0L);
        }
        this.isAgg = isAgg;
        // build a parse Node
        parse();
        // optimizer
    }

    @Override
    public String getSql() {
        return SemanticNode.getSql(parserNode);
    }

    @Override
    public String getSourceId() {
        return sourceId;
    }
}
