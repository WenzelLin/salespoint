package org.salespointframework.core.user;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

import org.salespointframework.util.Iterables;
import org.salespointframework.web.WebAuthenticationManager;

/**
 * 
 * @author Paul Henke
 *
 */
public class TransientUserManager implements UserManager<TransientUser> {

	private static final Map<UserIdentifier, TransientUser> userMap = new ConcurrentHashMap<>();
	//private static final Map<Object, TransientUser> userTokenMap = new ConcurrentHashMap<>();
	
	@Override
	public void add(TransientUser user) 
	{
		userMap.put(user.getIdentifier(), user);
	}

	@Override
	public boolean remove(UserIdentifier userIdentifier) 
	{
		// If user is logged on, log him off.
		/*
		for (Map.Entry<Object, TransientUser> entry : userTokenMap.entrySet()) {
			if (entry.getValue().getIdentifier().equals(userIdentifier)) {
				Object token = entry.getKey();
				this.logout(token);
				break;
			}
		}
		*/
		return userMap.remove(userIdentifier) != null;
	}

	@Override
	public boolean contains(UserIdentifier userIdentifier) 
	{
		return userMap.containsKey(userIdentifier);
	}

	@Override
	@Deprecated
	public void login(TransientUser user, Object token) 
	{
		Objects.requireNonNull(user, "user must not be null");
		Objects.requireNonNull(token, "token must not be null");
		
		// TODO
		WebAuthenticationManager.INSTANCE.login(user, (HttpSession) token);

		//userTokenMap.put(token, user);
	}

	@Override
	@Deprecated
	public void logout(Object token) 
	{
		Objects.requireNonNull(token, "token must not be null");
		
		WebAuthenticationManager.INSTANCE.logout((HttpSession) token);
		
		//userTokenMap.remove(token);
	}

	@Override
	@Deprecated
	public <E extends TransientUser> E getUserByToken(Class<E> clazz, Object token) 
	{
		Objects.requireNonNull(clazz, "clazz must not be null");
		Objects.requireNonNull(token, "token must not be null");

		User user = WebAuthenticationManager.INSTANCE.getUser((HttpSession) token);
		if(user == null)
			return null;
		return clazz.cast(user);
	}

	@Override
	public <E extends TransientUser> E get(Class<E> clazz, UserIdentifier userIdentifier) 
	{
		return clazz.cast(userMap.get(userIdentifier));
	}

	@Override
	public <E extends TransientUser> Iterable<E> find(Class<E> clazz) 
	{
		List<E> temp = new LinkedList<E>();
		for(TransientUser user : userMap.values()) 
		{
			if(clazz.isAssignableFrom(user.getClass()))
			{
				temp.add(clazz.cast(user));
			}
		}
		return Iterables.of(temp);
	}
}
