//******************************************************************************
//                              SQLQueryBuilder.java 
// SILEX-PHIS
// Copyright © INRA 2016
// Creation date: May 2016
// Contact: arnaud.charleroy@inra.fr, morgane.vidal@inra.fr, anne.tireau@inra.fr, 
//          pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.utils.sql;

import java.util.Iterator;
import java.util.List;

/**
 * SQL query builder.
 * @update [Andréas Garcia] 28 Feb. 2019: Add max clause handling
 * @author Morgane Vidal <morgane.vidal@inra.fr>, Arnaud Charleroy <arnaud.charleroy@inra.fr>
 */
public class SQLQueryBuilder {

    public static final String CONTAINS_OPERATOR = "CONTAINS_OPERATOR";
    
    private boolean count = false;
    private boolean max = false;
    private boolean distinct = false;
    private String attributes = null;
    public String from;
    public String where;
    public String join;
    public String orderBy;
    public String limit;
    public String offset;

    public SQLQueryBuilder() {
        where = "";
        from = "";
        join = "";
        orderBy = "";
        limit = "";
        offset = "";
    }

    public void appendOrderBy(String valuesList, String order) {
        if (valuesList != null) {
            orderBy += "\n" + valuesList + "\n";
            if (order != null) {
                orderBy += " " + order + " ";
            }
        }
    }

    public void appendCount() {
        count = true;
    }

    public void appendMax() {
        max = true;
    }

    public void appendDistinct() {
        distinct = true;
    }

    public void removeDistinct() {
        distinct = true;
    }

    public void removeCount() {
        count = false;
    }

    public void removeMax() {
        max = false;
    }

    public void addAND() {
        where += " AND ";
    }

    public void addOR() {
        where += " OR ";
    }

    /**
     * SELECT adding.
     * @param values
     */
    public void appendSelect(String values) {
        if (values != null) {
            attributes = values;
        }
    }

    public void addISNULL(String attribute) {
        if (where.length() > 0) {
                this.where += " AND ";
            }
        where += attribute + " IS NULL ";
    }
    
    public void appendJoin(String joinName, String table, String tableAlias, String constraint) {
        if (table != null && constraint != null && joinName != null) {
            join += "\n";
            if (tableAlias != null) {
                join += joinName + " \"" + table + "\" AS " + tableAlias;
            } else {
                join += joinName + " \"" + table + "\"";
            }
            if (!joinName.equals(JoinAttributes.NATURALJOIN)) {
                join += " ON " + constraint;
            }
        }
    }

    /**
     * FROM adding.
     * @param table
     * @param alias
     */
    public void appendFrom(String table, String alias) {
        if (table != null) {
            if (from.length() > 0) {
                from += ", \"" + table + "\" ";
            } else {
                from += " FROM \"" + table + "\" ";
            }
            if (alias != null) {
                from += "AS " + alias + " ";
            }
        }
    }

    public void appendORWhereConditions(String attribute, String value, String operator, String type, String tableAlias) {
        if (attribute != null && value != null) {
            if (CONTAINS_OPERATOR.equals(operator)) {
                operator = "ILIKE";
                value = "%" + value + "%";
            }
            if (where.length() > 0) {
                this.where += " OR ";
            }
            if (tableAlias != null) {
                this.where += tableAlias + "." + "\"" + attribute + "\"";
            } else {
                this.where += "\"" + attribute + "\"";
            }

            if (operator != null) {
                this.where += " " + operator + " ";
            } else {
                this.where += " = ";
            }
            this.where += "'" + value + "'";
            if (type != null) {
                this.where += type;
            }
        }
    }

    public void appendINConditions(String attribute, List<String> valuesGroup, String tableAlias) {
        if (valuesGroup != null) {
            if (where.length() > 0) {
                this.where += " AND ";
            }
            if (valuesGroup.size() > 1) {
                if (tableAlias != null) {
                    this.where += tableAlias + "." + "\"" + attribute + "\" IN (";
                } else {
                    this.where += "\"" + attribute + "\" IN (";
                }
                Iterator<String> it = valuesGroup.iterator();
                while (it.hasNext()) {
                    this.where += "'" + it.next() + "'";
                    if (it.hasNext()) {
                        this.where += ",";
                    }
                }
                this.where += " " + ") ";
            } else if (valuesGroup.size() == 1) {
                if (tableAlias != null) {
                    this.appendPrivateWhereConditions(attribute, valuesGroup.get(0), "=", null, tableAlias);
                } else {
                    this.appendPrivateWhereConditions(attribute, valuesGroup.get(0), "=", null, null);
                }
            }
        }
    }

    public void appendWhereConditions(String attribute, String value, String operator, String type, String tableAlias) {
        if (attribute != null && value != null) {
            if (CONTAINS_OPERATOR.equals(operator)) {
                operator = "ILIKE";
                value = "%" + value + "%";
            }
            if (tableAlias != null) {
                this.where += tableAlias + "." + "\"" + attribute + "\"";
            } else {
                this.where += "\"" + attribute + "\"";
            }

            if (operator != null) {
                this.where += " " + operator + " ";
            } else {
                this.where += " = ";
            }
            this.where += "'" + value + "'";
            if (type != null) {
                this.where += type;
            }
        }
    }

    public void appendANDWhereConditions(String attribute, String value, String operator, String type, String tableAlias) {
        if (attribute != null && value != null) {
            if (CONTAINS_OPERATOR.equals(operator)) {
                operator = "ILIKE";
                value = "%" + value + "%";
            }
            
            if (where.length() > 0) {
                this.where += " AND ";
            }
            if (tableAlias != null) {
                this.where += tableAlias + "." + "\"" + attribute + "\"";
            } else {
                this.where += "\"" + attribute + "\"";
            }

            if (operator != null) {
                this.where += " " + operator + " ";
            } else {
                this.where += " = ";
            }
            this.where += "'" + value + "'";
            if (type != null) {
                this.where += type;
            }
        }
    }
    
    /**
     * Adds WHERE (OR) condition if the value to test is not null.
     * @param attribute attribute name to test
     * @param value attribute value to test
     * @param operator comparison operator
     * @param type
     * @param tableAlias
     */
    public void appendORWhereConditionIfNeeded(String attribute, String value, String operator, String  type, String tableAlias) {
        if (value != null) {
            appendORWhereConditions(attribute, value, operator, type, tableAlias);
        }
    }
    
    /**
     * Adds WHERE (AND) condition if the value to test is not null.
     * @param attribute attribute name to test
     * @param value attribute value to test
     * @param operator comparison operator
     * @param type
     * @param tableAlias
     *
     */
    public void appendANDWhereConditionIfNeeded(String attribute, String value, String operator, String  type, String tableAlias) {
        if (value != null) {
            appendANDWhereConditions(attribute, value, operator, type, tableAlias);
        }
    }
    
    /**
     * Adds a LIMIT clause to the query.
     * @param limit limit value
     */
    public void appendLimit(String limit) {
        this.limit += limit;
    }
    
    public void appendOffset(String offset) {
        this.offset += offset;
    }
    
    public void appendANDWhereTableConstraintConditions(String attribute, String secondAttribute, String operator, String tableAlias, String secondTableAlias) {
        if (attribute != null && secondAttribute != null) {
            if (where.length() > 0) {
                this.where += " AND ";
            }
            if (tableAlias != null) {
                this.where += tableAlias + "." + "\"" + attribute + "\"";
            } else {
                this.where += "\"" + attribute + "\"";
            }

            if (operator != null) {
                this.where += " " + operator + " ";
            } else {
                this.where += " = ";
            }
            if (secondTableAlias != null) {
                this.where += secondTableAlias + "." + "\"" + secondAttribute + "\"";
            } else {
                this.where += "\"" + secondAttribute + "\"";
            }
        }
    }

    private void appendPrivateWhereConditions(String attribute, String value, String operator, String type, String tableAlias) {
        if (attribute != null && value != null) {
            if (tableAlias != null) {
                this.where += tableAlias + "." + "\"" + attribute + "\"";
            } else {
                this.where += "\"" + attribute + "\"";
            }

            if (operator != null) {
                this.where += " " + operator + " ";
            } else {
                this.where += " = ";
            }
            this.where += "'" + value + "'";
            if (type != null) {
                this.where += type;
            }
        }
    }

    @Override
    public String toString() {
        String query = "";
        if (attributes != null && attributes.length() > 0) {
            if (max) {
                query += "SELECT max(" + attributes + ") ";
            }
            else if (count) {
                if (distinct) {
                    query += "SELECT DISTINCT count(" + attributes + ") ";
                } else {
                    query += "SELECT count(" + attributes + ") ";
                }
            } else if (distinct) {
                query += "SELECT DISTINCT " + attributes + " ";
            } else {
                query += "SELECT " + attributes + " ";
            }
        } else if (count) {
            if (distinct) {
                query += "SELECT DISTINCT count(*) ";
            } else {
                query += "SELECT count(*) ";
            }

        } else if (distinct) {
            query += "SELECT DISTINCT * ";
        } else {
            query += "SELECT * ";
        }
        if (from != null) {
            query += from;
        }
        if (join != null) {
            query += join;
            query += "\n";
        }
        if (where != null && where.length() > 0) {
            query += "WHERE " + where;
        }
        if (orderBy != null && orderBy.length() > 0) {
            query += "\n";
            query += "ORDER BY ";
            query += orderBy;
        }
        if (limit != null && limit.length() > 0) {
            query += "\n";
            query += "LIMIT ";
            query += limit;
        }
        if (offset != null && offset.length() > 0) {
            query += "\n";
            query += "OFFSET ";
            query += offset;
        }
        return query;
    }
}
