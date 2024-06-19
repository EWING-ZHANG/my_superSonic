package com.tencent.supersonic.headless.core.parser.calcite;


import com.tencent.supersonic.headless.api.pojo.enums.EngineType;
import com.tencent.supersonic.headless.core.parser.calcite.schema.SemanticSqlTypeFactoryImpl;
import com.tencent.supersonic.headless.core.parser.calcite.schema.SemanticSqlDialect;
import com.tencent.supersonic.headless.core.parser.calcite.schema.ViewExpanderImpl;
import com.tencent.supersonic.headless.core.utils.SqlDialectFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.config.Lex;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.prepare.Prepare;
import org.apache.calcite.prepare.Prepare.CatalogReader;
import org.apache.calcite.rel.hint.HintStrategyTable;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.sql.SqlOperatorTable;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.impl.SqlParserImpl;
import org.apache.calcite.sql.util.ChainedSqlOperatorTable;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorScope;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;

/**
 * global configuration of the calcite
 */
public class Configuration {

    public static Properties configProperties = new Properties();
    public static RelDataTypeFactory typeFactory = new SemanticSqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT);
    public static SqlOperatorTable operatorTable = SqlStdOperatorTable.instance();
    public static CalciteConnectionConfig config = new CalciteConnectionConfigImpl(configProperties);

    public static SqlValidator.Config getValidatorConfig(EngineType engineType) {
        SemanticSqlDialect sqlDialect = SqlDialectFactory.getSqlDialect(engineType);
        return SqlValidator.Config.DEFAULT
                .withConformance(sqlDialect.getConformance())
                .withDefaultNullCollation(config.defaultNullCollation())
                .withLenientOperatorLookup(true);
    }

    static {
        configProperties.put(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), Boolean.TRUE.toString());
    }

    public static SqlParser.Config getParserConfig(EngineType engineType) {
        CalciteConnectionConfig config = new CalciteConnectionConfigImpl(configProperties);
        SemanticSqlDialect sqlDialect = SqlDialectFactory.getSqlDialect(engineType);

        SqlParser.ConfigBuilder parserConfig = SqlParser.configBuilder();
        parserConfig.setCaseSensitive(config.caseSensitive());
        parserConfig.setUnquotedCasing(config.unquotedCasing());
        parserConfig.setQuotedCasing(config.quotedCasing());
        parserConfig.setConformance(config.conformance());
        parserConfig.setLex(Lex.BIG_QUERY);
        parserConfig.setParserFactory(SqlParserImpl.FACTORY).setCaseSensitive(false)
                .setIdentifierMaxLength(Integer.MAX_VALUE)
                .setQuoting(Quoting.BACK_TICK)
                .setQuoting(Quoting.SINGLE_QUOTE)
                .setConformance(sqlDialect.getConformance())
                .setLex(Lex.BIG_QUERY);
        parserConfig = parserConfig.setQuotedCasing(Casing.TO_LOWER);
        parserConfig = parserConfig.setUnquotedCasing(Casing.TO_LOWER);
        return parserConfig.build();
    }

    public static SqlValidator getSqlValidator(CalciteSchema rootSchema, EngineType engineType) {
        List<SqlOperatorTable> tables = new ArrayList<>();
        tables.add(SqlStdOperatorTable.instance());
        SqlOperatorTable operatorTable = new ChainedSqlOperatorTable(tables);
        //operatorTable.
        Prepare.CatalogReader catalogReader = new CalciteCatalogReader(
                rootSchema,
                Collections.singletonList(rootSchema.getName()),
                typeFactory,
                config
        );
        return SqlValidatorUtil.newValidator(operatorTable, catalogReader, typeFactory,
                Configuration.getValidatorConfig(engineType));
    }

    public static SqlToRelConverter.Config getConverterConfig() {
        HintStrategyTable strategies = HintStrategyTable.builder().build();
        return SqlToRelConverter.config()
                .withHintStrategyTable(strategies)
                .withTrimUnusedFields(true)
                .withExpand(true)
                .addRelBuilderConfigTransform(c -> c.withSimplify(false));
    }

    public static SqlToRelConverter getSqlToRelConverter(SqlValidatorScope scope, SqlValidator sqlValidator,
            RelOptPlanner relOptPlanner, EngineType engineType) {
        RexBuilder rexBuilder = new RexBuilder(typeFactory);
        RelOptCluster cluster = RelOptCluster.create(relOptPlanner, rexBuilder);
        FrameworkConfig fromworkConfig = Frameworks.newConfigBuilder()
                .parserConfig(getParserConfig(engineType))
                .defaultSchema(scope.getValidator().getCatalogReader().getRootSchema().plus())
                .build();
        return new SqlToRelConverter(new ViewExpanderImpl(),
                sqlValidator,
                (CatalogReader) scope.getValidator().getCatalogReader(), cluster, fromworkConfig.getConvertletTable(),
                getConverterConfig());
    }

}
