package com.oou.has.bean;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.omnifaces.cdi.ViewScoped;
import org.omnifaces.util.Faces;
import org.omnifaces.util.Messages;

import com.oou.has.model.Role;
import com.oou.has.model.Status;
import com.oou.has.model.User;
import com.oou.has.service.UserService;

@Named
@ViewScoped
public class LoginBean implements Serializable {
    private String matricNumber;
    private String password;
    private String firstName;
    private String lastName;
    private String confirmPassword;
    private User user;
    private boolean isLogin = true;
    private static final String DASHBOARD_URL = "/HostelAllocationSystem/online/templates/master.xhtml?faces-redirect=true";
    private static final String ADMIN_URL = "/HostelAllocationSystem/online/dashboard/dashboard.xhtml?faces-redirect=true";

    @Inject
    private UserService userService; // EJB for user management

    // Getters and Setters
    public String getMatricNumber() {
        return matricNumber;
    }

    public void setMatricNumber(String matricNumber) {
        this.matricNumber = matricNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    // Fixed getter method name for boolean property
    public boolean getIsLogin() {
        return isLogin;
    }

    public void setIsLogin(boolean isLogin) {
        this.isLogin = isLogin;
    }

    // Alternative: you can also add this method for convenience
    public boolean isLogin() {
        return isLogin;
    }

    public String toggleForm() {
        isLogin = !isLogin;
        // Clear fields when toggling
        clearFields();
        return null;
    }

    private void clearFields() {
        matricNumber = null;
        password = null;
        firstName = null;
        lastName = null;
        confirmPassword = null;
    }

    public void login() {

    	try {
			Subject currentUser = SecurityUtils.getSubject();
			UsernamePasswordToken token = new UsernamePasswordToken(matricNumber, password);

			currentUser.login(token);
			System.out.println("Login successful");
			Subject subject = SecurityUtils.getSubject();
			User u = userService.findByUsername(matricNumber);
			this.user = u;
			subject.getSession().setAttribute("username", u.getMatricNumber());
			subject.getSession().setAttribute("useId", u.getId());
			subject.getSession().setAttribute("fullName", u.getFirstName()+ " "+ u.getLastName());
			if (!hasRole("SUPER_ADMIN")) {
			Faces.redirect(DASHBOARD_URL);
			}else {
				Faces.redirect(ADMIN_URL);
			}

		}catch (org.apache.shiro.authc.UnknownAccountException e) {
			signup();
			
		
		}
    	catch (Exception e) {
			e.printStackTrace();
			Messages.addFlashGlobalError("Authentication Failed!");
		}
    }

    public String signup() {
        // Validate password match

        User test = userService.findByUsername(matricNumber);
        if (test != null) {
        	Messages.addFlashGlobalError("Authentication Failed");
        	return null;

        }


        List<Role> roles = new ArrayList<Role>();
        roles.add(Role.STUDENT);
        User newUser = new User();
        newUser.setRoles(roles);
        newUser.setMatricNumber(matricNumber);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setPassword(password);
        newUser.setStatus(Status.UNVERIFIED);

        try {
            // Call EJB to create user
            userService.create(newUser);

            // Clear form and switch to login
            login();

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Account created successfully",
                "You can now sign in with your credentials."));

            return null; // Stay on the same page to show the login form

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Signup failed", ""));
            return null;
        }
    }

    public void logout() throws IOException {
		Subject currentUser = SecurityUtils.getSubject();
		currentUser.logout();
		Faces.invalidateSession();
		Faces.redirect("/HostelAllocationSystem/login.xhtml?faces-redirect=true");
	}

	public boolean isAuthenticated() {
		return SecurityUtils.getSubject().isAuthenticated();
	}

	public String getCurrentUser() {
		Subject currentUser = SecurityUtils.getSubject();
		if (currentUser.getPrincipal() != null) {
			return currentUser.getPrincipal() + "";
		}
		return "Guest";
	}

	private User getCurrentSession() {
		Subject subject = SecurityUtils.getSubject();
		return userService.findByUsername(matricNumber);
	}

	public boolean hasRole(String role) {
		Subject currentUser = SecurityUtils.getSubject();
		return currentUser.hasRole(role);

	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}