package de.jaxenter.eesummit.caroline.backend.tools;


import org.apache.commons.lang.Validate;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.Map;

/**
 * This class helps to build queries with conditional "AND" parts<br/>.
 * Tt is required as the JPA-1.0 Query interface will not allow to
 * alter the query String, so you have to know all parameters upfront
 * and then do all setParameter calls.
 * That leads to clumsy code that is now encapsulated in this class.
 *
 * In theory JPA-2.0 CriteriaBuilder should solve this problem, but it
 * creates unmaintainable sources if you make a bit more complicated
 * queries...
 */
public class QueryBuilder
{

    private Map<String, Object> parms = new HashMap<String, Object>();

    private StringBuilder sb;

    private boolean finished = false;

    private boolean firstAnd = true;

    private boolean whereUsed = false;


    /**
     * constructor with initial query part <br/>
     * e.g. "select from User as u"
     */
    public QueryBuilder(String initial)
    {
        Validate.notNull(initial);
        sb = new StringBuilder(initial);

        // check if the initial String already contains a 'where clause'
        whereUsed = (initial + " ").toUpperCase().contains(" WHERE ");
        firstAnd = !whereUsed;
    }

    /**
     * add "AND" query part with parameter <br/>
     * e.g. "u.name = :name", "name" , username
     */
    public QueryBuilder addQueryPart(String queryPart, String paramName, Object paramValue)
    {
        Validate.isTrue(!finished);
        Validate.notEmpty(queryPart);
        Validate.notEmpty(paramName);
        Validate.notNull(paramValue);
        addQueryPart(queryPart);
        parms.put(paramName, paramValue);

        return this;
    }

    /**
     * add "AND" query part without parameter
     */
    public QueryBuilder addQueryPart(String queryPart)
    {
        Validate.isTrue(!finished);
        Validate.notEmpty(queryPart);
        if (!whereUsed)
        {
            sb.append(" WHERE");
            whereUsed = true;
        }

        if (firstAnd)
        {
            firstAnd = false;
        }
        else if (!queryPart.toUpperCase().trim().startsWith("ORDER BY"))
        {
            sb.append(" AND");
        }
        sb.append(' ').append(queryPart);

        return this;
    }

    /**
     * will create query and call setParameter for all parameters
     */
    public Query getQuery(EntityManager em)
    {
        finished = true;
        Query q = em.createQuery(sb.toString());
        for (Map.Entry<String, Object> e : parms.entrySet())
        {
            q.setParameter(e.getKey(), e.getValue());
        }
        return q;
    }

    public String toString()
    {
        return sb.toString();
    }

}
