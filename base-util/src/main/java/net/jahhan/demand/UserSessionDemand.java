package net.jahhan.demand;

/**
 * @author nince
 */
public interface UserSessionDemand {

	public void login(Long loginRoleType,Long userId,String sessionId);
	
	public void logout(Long loginRoleType,Long userId,String sessionId);
	
	public void wslogin(Long loginRoleType,Long userId,String wsSessionId,String path);
	
	public void wslogout(Long loginRoleType,Long userId,String wsSessionId,String path);
	
}
