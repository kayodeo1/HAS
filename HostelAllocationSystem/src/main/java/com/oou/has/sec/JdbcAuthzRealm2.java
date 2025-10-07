package com.oou.has.sec;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.slf4j.Logger;
import javax.sql.DataSource;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.config.ConfigurationException;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.util.JdbcUtils;
import org.slf4j.LoggerFactory;

/**
 * Realm that allows authentication and authorization via JDBC calls. The
 * default queries suggest a potential schema for retrieving the user's password
 * for authentication, and querying for a user's roles and permissions. The
 * default queries can be overridden by setting the query properties of the
 * realm.
 * <p/>
 * If the default implementation of authentication and authorization cannot
 * handle your schema, this class can be subclassed and the appropriate methods
 * overridden. (usually
 * {@link #doGetAuthenticationInfo(org.apache.shiro.authc.AuthenticationToken)},
 * {@link #getRoleNamesForUser(java.sql.Connection,String)}, and/or
 * {@link #getPermissions(java.sql.Connection,String,java.util.Collection)}
 * <p/>
 * This realm supports caching by extending from
 * {@link org.apache.shiro.realm.AuthorizingRealm}.
 *
 * @since 0.2
 * @author AAfolayan and Apache Shiro
 */
public class JdbcAuthzRealm2 extends AuthorizingRealm {

	// TODO - complete JavaDoc

	/*--------------------------------------------
	|             C O N S T A N T S             |
	============================================*/
	/**
	 * The default query used to retrieve account data for the user.
	 */
	protected static final String DEFAULT_AUTHENTICATION_QUERY = "select password from users where matricNumber = ?";

	/**
	 * The default query used to retrieve account data for the user when
	 * {@link #saltStyle} is COLUMN.
	 */
	protected static final String DEFAULT_SALTED_AUTHENTICATION_QUERY = "select password, password_salt from users where username = ?";

	/**
	 * The default query used to retrieve the roles that apply to a user.
	 */
	protected static final String DEFAULT_USER_ROLES_QUERY = "select role_name from userroles where matricNumber = ?";

	/**
	 * The default query used to retrieve permissions that apply to a particular
	 * role.
	 */
	protected static final String DEFAULT_PERMISSIONS_QUERY = "select permission from roles_permissions where role_name = ?";

	private static final Logger log = LoggerFactory.getLogger(JdbcAuthzRealm2.class);

	/**
	 * Password hash salt configuration.
	 * <ul>
	 * <li>NO_SALT - password hashes are not salted.</li>
	 * <li>CRYPT - password hashes are stored in unix crypt format.</li>
	 * <li>COLUMN - salt is in a separate column in the database.</li>
	 * <li>EXTERNAL - salt is not stored in the database.
	 * {@link #getSaltForUser(String)} will be called to get the salt</li>
	 * </ul>
	 */
	public enum SaltStyle {
		NO_SALT, CRYPT, COLUMN, EXTERNAL
	}

	/*--------------------------------------------
	|    I N S T A N C E   V A R I A B L E S    |
	============================================*/
	protected DataSource dataSource;

	protected String authenticationQuery = DEFAULT_AUTHENTICATION_QUERY;

	protected String userRolesQuery = DEFAULT_USER_ROLES_QUERY;

	protected String permissionsQuery = DEFAULT_PERMISSIONS_QUERY;

	protected boolean permissionsLookupEnabled = false;

	protected SaltStyle saltStyle = SaltStyle.NO_SALT;

	/*--------------------------------------------
	|         C O N S T R U C T O R S           |
	============================================*/

	/*--------------------------------------------
	|  A C C E S S O R S / M O D I F I E R S    |
	============================================*/

	/**
	 * Sets the datasource that should be used to retrieve connections used by
	 * this realm.
	 *
	 * @param dataSource
	 *            the SQL data source.
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Overrides the default query used to retrieve a user's password during
	 * authentication. When using the default implementation, this query must
	 * take the user's username as a single parameter and return a single result
	 * with the user's password as the first column. If you require a solution
	 * that does not match this query structure, you can override
	 * {@link #doGetAuthenticationInfo(org.apache.shiro.authc.AuthenticationToken)}
	 * or just {@link #getPasswordForUser(java.sql.Connection,String)}
	 *
	 * @param authenticationQuery
	 *            the query to use for authentication.
	 * @see #DEFAULT_AUTHENTICATION_QUERY
	 */
	public void setAuthenticationQuery(String authenticationQuery) {
		this.authenticationQuery = authenticationQuery;
	}

	/**
	 * Overrides the default query used to retrieve a user's roles during
	 * authorization. When using the default implementation, this query must
	 * take the user's username as a single parameter and return a row per role
	 * with a single column containing the role name. If you require a solution
	 * that does not match this query structure, you can override
	 * {@link #doGetAuthorizationInfo(PrincipalCollection)} or just
	 * {@link #getRoleNamesForUser(java.sql.Connection,String)}
	 *
	 * @param userRolesQuery
	 *            the query to use for retrieving a user's roles.
	 * @see #DEFAULT_USER_ROLES_QUERY
	 */
	public void setUserRolesQuery(String userRolesQuery) {
		this.userRolesQuery = userRolesQuery;
	}

	/**
	 * Overrides the default query used to retrieve a user's permissions during
	 * authorization. When using the default implementation, this query must
	 * take a role name as the single parameter and return a row per permission
	 * with three columns containing the fully qualified name of the permission
	 * class, the permission name, and the permission actions (in that order).
	 * If you require a solution that does not match this query structure, you
	 * can override
	 * {@link #doGetAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection)}
	 * or just
	 * {@link #getPermissions(java.sql.Connection,String,java.util.Collection)}
	 * </p>
	 * <p/>
	 * <b>Permissions are only retrieved if you set
	 * {@link #permissionsLookupEnabled} to true. Otherwise, this query is
	 * ignored.</b>
	 *
	 * @param permissionsQuery
	 *            the query to use for retrieving permissions for a role.
	 * @see #DEFAULT_PERMISSIONS_QUERY
	 * @see #setPermissionsLookupEnabled(boolean)
	 */
	public void setPermissionsQuery(String permissionsQuery) {
		this.permissionsQuery = permissionsQuery;
	}

	/**
	 * Enables lookup of permissions during authorization. The default is
	 * "false" - meaning that only roles are associated with a user. Set this to
	 * true in order to lookup roles <b>and</b> permissions.
	 *
	 * @param permissionsLookupEnabled
	 *            true if permissions should be looked up during authorization,
	 *            or false if only roles should be looked up.
	 */
	public void setPermissionsLookupEnabled(boolean permissionsLookupEnabled) {
		this.permissionsLookupEnabled = permissionsLookupEnabled;
	}

	/**
	 * Sets the salt style. See {@link #saltStyle}.
	 *
	 * @param saltStyle
	 *            new SaltStyle to set.
	 */
	public void setSaltStyle(SaltStyle saltStyle) {
		this.saltStyle = saltStyle;
		if (saltStyle == SaltStyle.COLUMN && authenticationQuery.equals(DEFAULT_AUTHENTICATION_QUERY)) {
			authenticationQuery = DEFAULT_SALTED_AUTHENTICATION_QUERY;
		}
	}

	/*--------------------------------------------
	|               M E T H O D S               |
	============================================*/

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

		UsernamePasswordToken upToken = (UsernamePasswordToken) token;
		String username = upToken.getUsername();

		// Null username is invalid
		if (username == null) {
			throw new AccountException("Null usernames are not allowed by this realm.");
		}

		Connection conn = null;
		SimpleAuthenticationInfo info = null;
		try {
			conn = dataSource.getConnection();

			String password = null;
			String salt = null;
			switch (saltStyle) {
			case NO_SALT:
				password = getPasswordForUser(conn, username)[0];
				break;
			case CRYPT:
				// TODO: separate password and hash from getPasswordForUser[0]
				throw new ConfigurationException("Not implemented yet");
				// break;
			case COLUMN:
				String[] queryResults = getPasswordForUser(conn, username);
				password = queryResults[0];
				salt = queryResults[1];
				break;
			case EXTERNAL:
				password = getPasswordForUser(conn, username)[0];
				salt = getSaltForUser(username);
			}

			if (password == null) {
				throw new UnknownAccountException("No account found for user [" + username + "]");
			}

			info = new SimpleAuthenticationInfo(username, password.toCharArray(), getName());

			if (salt != null) {
				info.setCredentialsSalt(ByteSource.Util.bytes(salt));
			}

		} catch (SQLException e) {
			final String message = "There was a SQL error while authenticating user [" + username + "]";
			if (log.isErrorEnabled()) {
				log.error(message, e);
			}

			// Rethrow any SQL errors as an authentication exception
			throw new AuthenticationException(message, e);
		} finally {
			JdbcUtils.closeConnection(conn);
		}

		return info;
	}

	private String[] getPasswordForUser(Connection conn, String username) throws SQLException {

		String[] result;
		boolean returningSeparatedSalt = false;
		switch (saltStyle) {
		case NO_SALT:
		case CRYPT:
		case EXTERNAL:
			result = new String[1];
			break;
		default:
			result = new String[2];
			returningSeparatedSalt = true;
		}

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(authenticationQuery);
			ps.setString(1, username);

			// Execute query
			rs = ps.executeQuery();

			// Loop over results - although we are only expecting one result,
			// since usernames should be unique
			boolean foundResult = false;
			while (rs.next()) {

				// Check to ensure only one row is processed
				if (foundResult) {
					throw new AuthenticationException(
							"More than one user row found for user [" + username + "]. Usernames must be unique.");
				}

				result[0] = rs.getString(1);
				if (returningSeparatedSalt) {
					result[1] = rs.getString(2);
				}

				foundResult = true;
			}
		} finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeStatement(ps);
		}

		return result;
	}

	/**
	 * This implementation of the interface expects the principals collection to
	 * return a String username keyed off of this realm's {@link #getName()
	 * name}
	 *
	 * @see #getAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection)
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

		Subject subject = SecurityUtils.getSubject();
		String userName = principals.getPrimaryPrincipal().toString();
		//log.info("datasource injected >>> " + dataSource);
		//log.info("retrieving roles for >>> " + userName);

		AuthorizationInfo info = null;
		if (subject.isAuthenticated()) {
			Session session = subject.getSession();
			Object val = session.getAttribute("USER_ROLES");
			//log.info("session val >>> " + val);
			if (val == null) {
				info = doGetAuthorizationInfo2(principals);
				session.setAttribute("USER_ROLES", info);
				//log.info("returning fresh user roles! -> " + info);
			} else {
				//log.info("returning cached user roles hence avoiding db calls!");
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
		Set<String> permissions = null;
		try {
			conn = dataSource.getConnection();

			// Retrieve roles and permissions from database
			// log.info("Retrieve roles for user -> " + username);
			roleNames = getRoleNamesForUser(conn, username);
			// log.info("Retrieved roles -> " + roleNames);
			// if (permissionsLookupEnabled) {
			// permissions = getPermissions(conn, username, roleNames);
			// }

		} catch (SQLException e) {
			final String message = "There was a SQL error while authorizing user [" + username + "]";
			if (log.isErrorEnabled()) {
				log.error(message, e);
			}

			// Rethrow any SQL errors as an authorization exception
			throw new AuthorizationException(message, e);
		} finally {
			JdbcUtils.closeConnection(conn);
		}

		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roleNames);
		log.info("roles returned for user " + username + " >>> " + roleNames);
		// info.setStringPermissions(permissions);
		return info;

	}

	protected Set<String> getRoleNamesForUser(Connection conn, String username) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Set<String> roleNames = new LinkedHashSet<>();
		try {
			ps = conn.prepareStatement(userRolesQuery);
			ps.setString(1, username);

			// Execute query
			rs = ps.executeQuery();

			// Loop over results and add each returned role to a set
			while (rs.next()) {

				String roleName = rs.getString(1);

				// Add the role to the list of names if it isn't null
				if (roleName != null) {
					roleNames.add(roleName);
				} else {
					if (log.isWarnEnabled()) {
						log.warn("Null role name found while retrieving role names for user [" + username + "]");
					}
				}
			}
		} finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeStatement(ps);
		}
		return roleNames;
	}

	protected Set<String> getPermissions(Connection conn, String username, Collection<String> roleNames)
			throws SQLException {
		PreparedStatement ps = null;
		Set<String> permissions = new LinkedHashSet<>();
		try {
			ps = conn.prepareStatement(permissionsQuery);
			for (String roleName : roleNames) {

				ps.setString(1, roleName);

				ResultSet rs = null;

				try {
					// Execute query
					rs = ps.executeQuery();

					// Loop over results and add each returned role to a set
					while (rs.next()) {

						String permissionString = rs.getString(1);

						// Add the permission to the set of permissions
						permissions.add(permissionString);
					}
				} finally {
					JdbcUtils.closeResultSet(rs);
				}

			}
		} finally {
			JdbcUtils.closeStatement(ps);
		}

		return permissions;
	}

	protected String getSaltForUser(String username) {
		return username;
	}

}
