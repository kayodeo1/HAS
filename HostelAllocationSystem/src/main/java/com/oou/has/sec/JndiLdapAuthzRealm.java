/**
 *
 */
package com.oou.has.sec;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oou.has.model.Status;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.ldap.JndiLdapRealm;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.JdbcUtils;
import org.slf4j.LoggerFactory;



/**
 * @author AAfolayan
 *
 */
public class JndiLdapAuthzRealm extends JndiLdapRealm {

	protected DataSource dataSource;
	protected String userRolesQuery;
	private static Logger LOG = LoggerFactory.getLogger(JndiLdapAuthzRealm.class);

	public JndiLdapAuthzRealm() {
		super();
		LOG.info("INSTANTIATING JndiLdapAuthzRealm instance...");
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		LOG.info("Authenticating user >>> " + token.getPrincipal());
		AuthenticationInfo info = super.doGetAuthenticationInfo(token);
		// check if user is enabled on db
		LOG.info("user status to check >>> " + token.getPrincipal());
		try {
			boolean enabled = isUserEnabled(token.getPrincipal() + "");
			if (enabled) {
				return info;
			} else {
				throw new AuthenticationException(
						token.getPrincipal() + " status is disabled or couldn't be found on the repository!");
			}

		} catch (SQLException e) {
			LOG.error("oops error encountered while checking user's status!", e.fillInStackTrace());
			e.printStackTrace();
			throw new AuthenticationException("oops error encountered while checking user status!",
					e.fillInStackTrace());
		}
	}

	@Override
	protected AuthorizationInfo queryForAuthorizationInfo(PrincipalCollection principals,
			LdapContextFactory ldapContextFactory) throws NamingException {

		Subject subject = SecurityUtils.getSubject();
		// LOG.info("subject >>> " + subject);
		// LOG.info("subject info >>> " + subject.getPrincipal());
		// LOG.info("subject auth? >>> " + subject.isAuthenticated());
		// LOG.info("subject session >>> " + subject.getSession());

		// SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		String userName = principals.getPrimaryPrincipal().toString();
		// LOG.info("datasource injected >>> " + dataSource);
		// LOG.info("retrieving roles for >>> " + userName);
		// info.addRoles(getRoles(userName));
		// info.addStringPermissions(getPermissions(userName));
		// return info;

		AuthorizationInfo info = null;
		if (subject.isAuthenticated()) {
			Session session = subject.getSession();
			Object val = session.getAttribute("USER_ROLE");
			// LOG.info("session val >>> " + val);
			if (val == null) {
				info = doGetAuthorizationInfo2(principals);
				session.setAttribute("USER_ROLE", info);
				// LOG.info("returning fresh user roles!");
			} else {
				// LOG.info("returning cached user roles hence avoiding db calls!");
				info = (AuthorizationInfo) val;
			}

		}
		return info;

	}

	private AuthorizationInfo doGetAuthorizationInfo2(PrincipalCollection principals) {

		// null usernames are invalid
		if (principals == null) {
			throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
		}

		String username = (String) getAvailablePrincipal(principals);

		Connection conn = null;
		Set<String> roleNames = null;
		// Set<String> permissions = null;
		try {
			conn = dataSource.getConnection();
			// LOG.info("connection from ds >>> " + conn);

			// Retrieve roles and permissions from database
			roleNames = getRoleNamesForUser(conn, username);

		} catch (SQLException e) {
			final String message = "There was a SQL error while authorizing user [" + username + "]";
			LOG.error(message, e);

			// Rethrow any SQL errors as an authorization exception
			throw new AuthorizationException(message, e);
		} finally {
			JdbcUtils.closeConnection(conn);
		}

		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roleNames);
		// info.setStringPermissions(permissions);
		LOG.info("roles returned for user " + username + " >>> " + roleNames);
		return info;

	}

	protected boolean isUserEnabled(String username) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		boolean result = false;
		try {
			conn = dataSource.getConnection();
//			ps = conn.prepareStatement("select id from public2.user where username = ? and status=?");
			ps = conn.prepareStatement("select id from user_ where username = ? and status=?");
			ps.setString(1, username);
			ps.setString(2, Status.ENABLED + "");
			// ps.setString(1, "deal");

			// Execute query
			rs = ps.executeQuery();

			// Loop over results and add each returned role to a set
			if (rs.next()) {
				LOG.info("user " + username + " is enabled!");
				result = true;
			} else {
				LOG.info("user " + username + " is not enabled or cannot be found!");
				result = false;
			}
		} finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeStatement(ps);
			JdbcUtils.closeConnection(conn);
		}
		return result;
	}

	protected Set<String> getRoleNamesForUser(Connection conn, String username) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Set<String> roleNames = new LinkedHashSet<>();
		try {
			ps = conn.prepareStatement(userRolesQuery);
			ps.setString(1, username);
			ps.setString(2, Status.ENABLED + "");
			// ps.setString(1, "deal");

			// Execute query
			rs = ps.executeQuery();

			// Loop over results and add each returned role to a set
			while (rs.next()) {

				String roleName = rs.getString(1);

				// Add the role to the list of names if it isn't null
				if (roleName != null) {
					roleNames.add(roleName);
				} else {
					LOG.warn("Null role name found while retrieving role names for user [" + username + "]");
				}
			}
		} finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeStatement(ps);
		}
		return roleNames;
	}

	/**
	 * @return the userRolesQuery
	 */
	public String getUserRolesQuery() {
		return userRolesQuery;
	}

	/**
	 * @param userRolesQuery the userRolesQuery to set
	 */
	public void setUserRolesQuery(String userRolesQuery) {
		this.userRolesQuery = userRolesQuery;
	}

	/**
	 * @return the dataSource
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * @param dataSource the dataSource to set
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

}