/**
 *
 */
package com.oou.has.model;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * @author AAfolayan
 */

@MappedSuperclass
public class AbstractEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	// @GeneratedValue(generator = "user_id_seq", strategy =
	// GenerationType.SEQUENCE)
	// @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id_seq",
	// schema = "public2", allocationSize = 1)
	@Column(name = "id")
	protected Long id;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> objectAsMap = new HashMap<>();
		try {
			BeanInfo info = Introspector.getBeanInfo(this.getClass());
			for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
				Method reader = pd.getReadMethod();
				if (reader != null) {
					objectAsMap.put(pd.getName(), reader.invoke(this));
				}
			}
		} catch (IntrospectionException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// when opps happens
			e.printStackTrace();
		}
		return objectAsMap;
	}
}