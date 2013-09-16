package org.jenkinsci.plugins.ghprb;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

/**
 * @author janinko
 */
public class GhprbGitHub {
	private static final Logger logger = Logger.getLogger(GhprbGitHub.class.getName());
	private GitHub gh;
	private GitHub ghStatus;

	private void connect() throws IOException{
		String accessToken = GhprbTrigger.getDscp().getAccessToken();
		String serverAPIUrl = GhprbTrigger.getDscp().getServerAPIUrl();
		if(accessToken != null && !accessToken.isEmpty()) {
			try {
				gh = GitHub.connectUsingOAuth(serverAPIUrl, accessToken);
			} catch(IOException e) {
				logger.log(Level.SEVERE, "Can''t connect to {0} using oauth", serverAPIUrl);
				throw e;
			}
		} else {
			gh = GitHub.connect(GhprbTrigger.getDscp().getUsername(), null, GhprbTrigger.getDscp().getPassword());
		}
	}

	private void connectStatus() throws IOException{
		String statusAccessToken = GhprbTrigger.getDscp().getStatusAccessToken();
		String serverAPIUrl = GhprbTrigger.getDscp().getServerAPIUrl();
		try {
			ghStatus = GitHub.connectUsingOAuth(serverAPIUrl, statusAccessToken);
		} catch(IOException e) {
			logger.log(Level.SEVERE, "Can''t connect to {0} using oauth", serverAPIUrl);
			throw e;
		}
	}

	public GitHub get() throws IOException{
		if(gh == null){
			connect();
		}
		return gh;
	}

	public GitHub getStatus() throws IOException{
		String statusAccessToken = GhprbTrigger.getDscp().getStatusAccessToken();

		if(statusAccessToken != null && !statusAccessToken.isEmpty()) {
			if (ghStatus == null){
				connectStatus();
			}
			return ghStatus;
		}

		get();
		return gh;
	}

	public boolean isUserMemberOfOrganization(String organisation, String member){
		try {
			GHOrganization org = get().getOrganization(organisation);
			List<GHUser> members = org.getMembers();
			for(GHUser user : members){
				if(user.getLogin().equals(member)){
					return true;
				}
			}
		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
			return false;
		}
		return false;
	}
}
