package giis.tdrules.model.io;

import static giis.tdrules.model.ModelUtil.safe;

import giis.portable.xml.tiny.XNode;
import giis.tdrules.model.ModelUtil;
import giis.tdrules.openapi.model.QueryEntitiesBody;
import giis.tdrules.openapi.model.QueryParam;
import giis.tdrules.openapi.model.QueryParametersBody;
import giis.tdrules.openapi.model.TdRule;
import giis.tdrules.openapi.model.TdRules;
import giis.tdrules.openapi.model.TdRulesBody;

/**
 * Custom xml serialization/deserialization of a rules model
 */
public class TdRulesXmlSerializer extends BaseXmlSerializer {

	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	private static final String SQLFPC = "sqlfpc";
	private static final String SQLMUTATION = "sqlmutation";
	private static final String VERSION = "version";
	private static final String DEVELOPMENT = "development";
	private static final String PARSEDSQL = "parsedsql";
	private static final String ERROR = "error";
	
	public TdRules deserialize(String xml) {
		XNode xtdrules=new XNode(xml);
		TdRules tdrules=new TdRules();
		String rulesClass=xtdrules.name();
		if ("sqlfpcws".equals(rulesClass))
			rulesClass=SQLFPC;
		else if ("sqlmutationws".equals(rulesClass))
			rulesClass=SQLMUTATION;
		if (!SQLMUTATION.equals(rulesClass) && !SQLFPC.equals(rulesClass))
			throw new RuntimeException("Root element must be sqlmutation or sqlfpc"); //NOSONAR

		tdrules.setRulesClass(rulesClass);
		tdrules.setVersion(getElemAttribute(xtdrules, VERSION));
		tdrules.setEnvironment(xtdrules.getChild(VERSION).getChild(DEVELOPMENT) == null ? "" : DEVELOPMENT);
		for (String attr : getExtendedAttributeNames(xtdrules, new String[] {}))
			tdrules.putSummaryItem(attr, xtdrules.getAttribute(attr));

		tdrules.setQuery(getElemAttribute(xtdrules, "sql"));
		tdrules.setParsedquery(getElemAttribute(xtdrules, PARSEDSQL));
		tdrules.setError(getElemAttribute(xtdrules, ERROR));
		
		String ruleTag=rulesClassToRuleTag(rulesClass);
		XNode xrules=xtdrules.getChild(ruleTag+"s");
		if (xrules==null)
			return tdrules;
		for (XNode rnode : xrules.getChildren(ruleTag)) {
			TdRule rule=new TdRule();
			for (String attr : getExtendedAttributeNames(rnode, new String[] {}))
				rule.putSummaryItem(attr, rnode.getAttribute(attr));
			rule.setId(getElemAttribute(rnode, "id"));
			rule.setCategory(getElemAttribute(rnode, "category"));
			rule.setMaintype(getElemAttribute(rnode, "type"));
			rule.setSubtype(getElemAttribute(rnode, "subtype"));
			rule.setLocation(getElemAttribute(rnode, "location"));
			rule.setEquivalent(rnode.getChild("equivalent")==null ? "" : "true");
			rule.setQuery(getElemAttribute(rnode, "sql"));
			rule.setDescription(getElemAttribute(rnode, "description"));
			rule.setError(getElemAttribute(rnode, ERROR));
			tdrules.addRulesItem(rule);
		}
		return tdrules;
	}
	private String rulesClassToRuleTag(String rulesClass) {
		return SQLMUTATION.equals(rulesClass) ? "mutant" : "fpcrule";
	}
	
	public String serialize(TdRules sqr) {
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
		sb.append(setElemAttribute(0, "sql", sqr.getQuery()))
			.append(setElemAttribute(0, PARSEDSQL, sqr.getParsedquery()));
		sb.append(setElemAttribute(0, ERROR, sqr.getError()));
		
		String ruleTag=rulesClassToRuleTag(rulesClass);
		if ("".equals(sqr.getError())) { //no muestra tag si ha habido error generando
			sb.append("\n<" + ruleTag + "s>");
			for (TdRule rule : safe(sqr.getRules()))
				sb.append("\n").append(serialize(rule, ruleTag));
			sb.append("\n</" + ruleTag + "s>");
		}
		sb.append("\n</" + rulesClass + ">");
		return sb.toString();
	}
	public String serialize(TdRule rule, String ruleTag) {
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
			.append(setElemAttribute(4, "sql", rule.getQuery()))
			.append(setElemAttribute(4, "description", rule.getDescription()))
			.append(setElemAttribute(4, ERROR, rule.getError()))
			.append("\n  </" + ruleTag + ">");
		return sb.toString();
	}
	
	//Deserializacion de otros objetos obtenidos de los servicios TdRules
	//Aunque en v2 se devuelve una estructura similar a las reglas que incluye version, entorno, etc.
	//para la v3 y 4 se simplificara, y solo se tiene el error (uno solo) o el contenido objeto a devolver
	
	public QueryEntitiesBody deserializeEntities(String xml) {
		XNode xtables=new XNode(xml);
		QueryEntitiesBody tables=new QueryEntitiesBody();
		tables.setError(getElemAttribute(xtables, ERROR));
		for (XNode xtable : xtables.getChildren("table"))
			tables.addEntitiesItem(XNode.decodeText(xtable.innerText()));
		return tables;
	}
	public QueryParametersBody deserializeParameters(String xml) {
		XNode xparams=new XNode(xml);
		QueryParametersBody sparams=new QueryParametersBody();
		sparams.setError(getElemAttribute(xparams, ERROR));
		XNode paramNode=xparams.getChild("parameters");
		if (paramNode==null)
			return sparams;
		for (XNode xparam : paramNode.getChildren("parameter")) {
		    QueryParam param=new QueryParam();
		    param.setName(xparam.getAttribute("name"));
		    param.setValue(xparam.getAttribute("value"));
			sparams.addParametersItem(param);
		}
		return sparams;
	}
	public String serialize(QueryEntitiesBody model) {
		StringBuilder sb=new StringBuilder();
		sb.append(XML_HEADER)
			.append("\n<sqltables>");
		if ("".equals(model.getError()))
			for (String table : ModelUtil.safe(model.getEntities()))
				sb.append(setElemAttribute(0, "table", table));
		else
			sb.append(setElemAttribute(0, ERROR, model.getError()));
		sb.append("\n</sqltables>");
		return sb.toString();
	}
	public String serialize(QueryParametersBody model) {
		StringBuilder sb=new StringBuilder();
		sb.append(XML_HEADER)
			.append("\n<sqlparameters>");
		if ("".equals(model.getError())) {
    		sb.append("\n<parameters>");
        	for (QueryParam param : ModelUtil.safe(model.getParameters()))
        		sb.append("\n<parameter")
    				.append(setAttribute("name",param.getName()))
    				.append(setAttribute("value",param.getValue()))
    				.append(" />");
        	sb.append("\n</parameters>")
        		.append(setElemAttribute(0, PARSEDSQL, model.getParsedquery()));
		} else {
			sb.append(setElemAttribute(0, ERROR, model.getError()));
		}
    	sb.append("\n</sqlparameters>");
		return sb.toString();
	}

	public String serialize(TdRulesBody sqb) {
		StringBuilder sb=new StringBuilder();
		sb.append("<body>");
		sb.append(setElemAttribute(0, "sql", sqb.getQuery()));
		if (sqb.getSchema()!=null)
			sb.append("\n").append(new TdSchemaXmlSerializer().serialize(sqb.getSchema()));
		sb.append(setElemAttribute(0, "options", sqb.getOptions()));
		sb.append("\n</body>");
		return sb.toString();
	}

}
