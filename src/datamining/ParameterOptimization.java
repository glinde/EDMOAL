/**
 * TODO File Description
 */
package datamining;

/**
 * TODO Class Description
 *
 * @author Roland Winkler
 */
public interface ParameterOptimization<P>
{
	public P getParameter();
	
	public void updateParameter(P parameter);
	
	public void initializeWith(P initialParameter);
}
