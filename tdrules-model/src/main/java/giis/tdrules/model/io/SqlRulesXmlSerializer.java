package giis.tdrules.model.io;

import static giis.tdrules.model.ModelUtil.safe;

import giis.portable.xml.tiny.XNode;
import giis.tdrules.model.ModelUtil;
import giis.tdrules.openapi.model.SqlParam;
import giis.tdrules.openapi.model.SqlParametersBody;
import giis.tdrules.openapi.model.SqlRule;
import giis.tdrules.openapi.model.SqlRules;
import giis.tdrules.openapi.model.SqlRulesBody;
import giis.tdrules.openapi.model.SqlTableListBody;

/**
 * Custom xml serialization/deserialization of a rules model
 */
public class SqlRulesXmlSerializer extends BaseXmlSerializer {

	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	private static final String SQLFPC = "sqlfpc";
	private static final String SQLMUTATION = "sqlmutation";
	private static final String VERSION = "version";
	private static final String DEVELOPMENT = "development";
	private static final String PARSEDSQL = "parsedsql";
	private static final String ERROR = "error";
	
	public SqlRules deserialize(String xml) {
		XNode xsqlrules=new XNode(xml);
		SqlRules sqlrules=new SqlRules();
		String rulesClass=xsqlrules.name();
		if ("sqlfpcws".equals(rulesClass))
			rulesClass=SQLFPC;
		else if ("sqlmutationws".equals(rulesClass))
			rulesClass=SQLMUTATION;
		if (!SQLMUTATION.equals(rulesClass) && !SQLFPC.equals(rulesClass))
			throw new RuntimeException("Root element must be sqlmutation or sqlfpc"); //NOSONAR

		sqlrules.setRulesClass(rulesClass);
		sqlrules.setVersion(getElemAttribute(xsqlrules, VERSION));
		sqlrules.setEnvironment(xsqlrules.getChild(VERSION).getChild(DEVELOPMENT) == null ? "" : DEVELOPMENT);
		for (String attr : getExtendedAttributeNames(xsqlrules, new String[] {}))
			sqlrules.putSummaryItem(attr, xsqlrules.getAttribute(attr));

		sqlrules.setSql(getElemAttribute(xsqlrules, "sql"));
		sqlrules.setParsedsql(getElemAttribute(xsqlrules, PARSEDSQL));
		sqlrules.setError(getElemAttribute(xsqlrules, ERROR));
		
		String ruleTag=rulesClassToRuleTag(rulesClass);
		XNode xrules=xsqlrules.getChild(ruleTag+"s");
		if (xrules==null)
			return sqlrules;
		for (XNode rnode : xrules.getChildren(ruleTag)) {
			SqlRule rule=new SqlRule();
			for (String attr : getExtendedAttributeNames(rnode, new String[] {}))
				rule.putSummaryItem(attr, rnode.getAttribute(attr));
			rule.setId(getElemAttribute(rnode, "id"));
			rule.setCategory(getElemAttribute(rnode, "category"));
			rule.setMaintype(getElemAttribute(rnode, "type"));
			rule.setSubtype(getElemAttribute(rnode, "subtype"));
			rule.setLocation(getElemAttribute(rnode, "location"));
			rule.setEquivalent(rnode.getChild("equivalent")==null ? "" : "true");
			rule.setSql(getElemAttribute(rnode, "sql"));
			rule.setDescription(getElemAttribute(rnode, "description"));
			rule.setError(getElemAttribute(rnode, ERROR));
			sqlrules.addRulesItem(rule);
		}
		return sqlrules;
	}
	private String rulesClassToRuleTag(String rulesClass) {
		return SQLMUTATION.equals(rulesClass) ? "mutant" : "fpcrule";
	}
	
	public String serialize(SqlRules sqr) {
		StringBuilder sb=new StringBuilder();
		String rulesClass=sqr.getRulesClass();
		sb.append(XML_HEADER)
			.append("\n<" + rulesClass)
			.append(setExtendedAttributes(sqr.getSummary()))
			.append(">");
		sb.append("\n<version>")
			.append(sqr.getVersion())
			.append(DEVELOPMENT.equals(sqr.getEnvironment()) ? "<development/>" : "")
			.append("</version>");
		sb.append(setElemAttribute(0, "sql", sqr.getSql()))
			.append(setElemAttribute(0, PARSEDSQL, sqr.getParsedsql()));
		sb.append(setElemAttribute(0, ERROR, sqr.getError()));
		
		String ruleTag=rulesClassToRuleTag(rulesClass);
		if ("".equals(sqr.getError())) { //no muestra tag si ha habido error generando
			sb.append("\n<" + ruleTag + "s>");
			for (SqlRule rule : safe(sqr.getRules()))
				sb.append("\n").append(serialize(rule, ruleTag));
			sb.append("\n</" + ruleTag + "s>");
		}
		sb.append("\n</" + rulesClass + ">");
		return sb.toString();
	}
	public String serialize(SqlRule rule, String ruleTag) {
		StringBuilder sb=new StringBuilder();
		sb.append("  <" + ruleTag)
			.append(setExtendedAttributes(rule.getSummary()))
			.append(">");
		sb.append(setElemAttribute("id", rule.getId()))
			.append(setElemAttribute("category", rule.getCategory()))
			.append(setElemAttribute("type", rule.getMaintype()))
			.append(setElemAttribute("subtype", rule.getSubtype()))
			.append(setElemAttribute("location", rule.getLocation()))
			.append("true".equals(rule.getEquivalent()) ? "\n    <equivalent/>" : "")
			.append(setElemAttribute(4, "sql", rule.getSql()))
			.append(setElemAttribute(4, "description", rule.getDescription()))
			.append(setElemAttribute(4, ERROR, rule.getError()))
			.append("\n  </" + ruleTag + ">");
		return sb.toString();
	}
	
	//Deserializacion de otros objetos obtenidos de los servicios SqlRules
	//Aunque en v2 se devuelve una estructura similar a las reglas que incluye version, entorno, etc.
	//para la v3 se simplificara, y solo se tiene el error (uno solo) o el contenido objeto a devolver
	
	public SqlTableListBody deserializeTables(String xml) {
		XNode xtables=new XNode(xml);
		SqlTableListBody tables=new SqlTableListBody();
		tables.setError(getElemAttribute(xtables, ERROR));
		for (XNode xtable : xtables.getChildren("table"))
			tables.addTablesItem(XNode.decodeText(xtable.innerText()));
		return tables;
	}
	public SqlParametersBody deserializeParameters(String xml) {
		XNode xparams=new XNode(xml);
		SqlParametersBody sparams=new SqlParametersBody();
		sparams.setError(getElemAttribute(xparams, ERROR));
		XNode paramNode=xparams.getChild("parameters");
		if (paramNode==null)
			return sparams;
		for (XNode xparam : paramNode.getChildren("parameter")) {
		    SqlParam param=new SqlParam();
		    param.setName(xparam.getAttribute("name"));
		    param.setValue(xparam.getAttribute("value"));
			sparams.addParametersItem(param);
		}
		return sparams;
	}
	public String serialize(SqlTableListBody model) {
		StringBuilder sb=new StringBuilder();
		sb.append(XML_HEADER)
			.append("\n<sqltables>");
		if ("".equals(model.getError()))
			for (String table : ModelUtil.safe(model.getTables()))
				sb.append(setElemAttribute(0, "table", table));
		else
			sb.append(setElemAttribute(0, ERROR, model.getError()));
		sb.append("\n</sqltables>");
		return sb.toString();
	}
	public String serialize(SqlParametersBody model) {
		StringBuilder sb=new StringBuilder();
		sb.append(XML_HEADER)
			.append("\n<sqlparameters>");
		if ("".equals(model.getError())) {
    		sb.append("\n<parameters>");
        	for (SqlParam param : ModelUtil.safe(model.getParameters()))
        		sb.append("\n<parameter")
    				.append(setAttribute("name",param.getName()))
    				.append(setAttribute("value",param.getValue()))
    				.append(" />");
        	sb.append("\n</parameters>")
        		.append(setElemAttribute(0, PARSEDSQL, model.getParsedsql()));
		} else {
			sb.append(setElemAttribute(0, ERROR, model.getError()));
		}
    	sb.append("\n</sqlparameters>");
		return sb.toString();
	}

	public String serialize(SqlRulesBody sqb) {
		StringBuilder sb=new StringBuilder();
		sb.append("<body>");
		sb.append(setElemAttribute(0, "sql", sqb.getSql()));
		if (sqb.getSchema()!=null)
			sb.append("\n").append(new DbSchemaXmlSerializer().serialize(sqb.getSchema()));
		sb.append(setElemAttribute(0, "options", sqb.getOptions()));
		sb.append("\n</body>");
		return sb.toString();
	}

}
