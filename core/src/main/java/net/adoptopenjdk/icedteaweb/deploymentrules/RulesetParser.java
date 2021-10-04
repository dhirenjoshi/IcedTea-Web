package net.adoptopenjdk.icedteaweb.deploymentrules;

import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.adoptopenjdk.icedteaweb.xmlparser.XmlNode;

import java.util.ArrayList;
import java.util.List;

import static net.adoptopenjdk.icedteaweb.xmlparser.NodeUtils.getAttribute;
import static net.adoptopenjdk.icedteaweb.xmlparser.NodeUtils.getChildNodes;


/**
 * Contains methods to parse an XML document into a DeploymentRuleSetFile.
 * Implements JNLP specification version 1.0.
 *
 * See DTD: https://docs.oracle.com/javase/8/docs/technotes/guides/deploy/deployment_rules.html#CIHBAJBB
 * See: https://docs.oracle.com/javase/8/docs/technotes/guides/deploy/deployment_rules.html#CIHGIDJI
 */
class RulesetParser {

    private static final String ID_ELEMENT = "id";
    private static final String RULE_SET_ELEMENT = "ruleset";

    //From rule starts the actual list of rule and locations stored.
    private static final String RULE_ELEMENT = "rule";
    private static final String ACTION_ELEMENT = "action";
    //id element
    private static final String LOCATION_ATTRIBUTE = "location";
    //certificate element
    private static final String HASH_ATTRIBUTE = "hash";
    //action element
    private static final String VERSION_ATTRIBUTE = "version";
    private static final String PERMISSION_ATTRIBUTE = "permission";

    /**
     * Create a parser for the Deployment rule set file
     * Reads the jar and ruleset.xml file is read and parsed. Adds a deploymentRuleSet tag to cover the legalities
     * If any with using a Oracle ruleset.xml.
     * <p>
     *
     * @param root     the root XmlNode
     * @throws ParseException if the  DeploymentRuleSet string is invalid
     */
    public List<Rule> getRules(final XmlNode root) throws ParseException {
        // ensure it's a DeploymentRuleSet node
        if (root == null || !root.getNodeName().equals(RULE_SET_ELEMENT)) {
            throw new ParseException("Root element is not a <" + RULE_SET_ELEMENT + "> element.");
        }
        return getRulesFromRuleset(root);
    }

    /**
     * Extracts rules from the xml tree.
     * The {@code version} attribute of the {@code ruleset} element is ignored
     * since there will be no new version of the schema in the future.
     *
     * @param parent the root node
     * @return all rules found.
     * @throws ParseException if the xml is not valid.
     */
    private List<Rule> getRulesFromRuleset(final XmlNode parent) throws ParseException {
        final List<Rule> result = new ArrayList<>();
        final XmlNode[] rules = getChildNodes(parent, RULE_ELEMENT);

        if (rules.length == 0) {
            throw new ParseException("No  rule <rule> element specified.");
        }

        for (final XmlNode rule : rules) {
            result.add(getRule(rule));
        }
        return result;
    }

    private Rule getRule(final XmlNode node) {

        // create rules
        final Rule rule = new Rule();

        // step through the elements
        // first populate the id tag attribute
        final XmlNode potentialIdPart = node.getFirstChild();
        if (potentialIdPart.getNodeName().equals(ID_ELEMENT)) {
            //certificate element
            final String hash = getAttribute(potentialIdPart, HASH_ATTRIBUTE, null);
            //id element
            final Certificate certs = new Certificate();
            certs.setHash(hash);

            final String location = getAttribute(potentialIdPart, LOCATION_ATTRIBUTE, null);
            rule.setCertificate(certs);
            rule.setLocation(location);
        }

        // next populate the action tag attribute.
        final XmlNode potentialActionPart = potentialIdPart.getNextSibling();
        if (potentialActionPart.getNodeName().equals(ACTION_ELEMENT)) {
            final Action action = new Action();
            //action element
            final String permission = getAttribute(potentialActionPart, PERMISSION_ATTRIBUTE, null);
            final String version = getAttribute(potentialActionPart, VERSION_ATTRIBUTE, null);
            action.setPermission(permission);
            action.setVersion(version);
            rule.setAction(action);
        }

        return rule;
    }
}
