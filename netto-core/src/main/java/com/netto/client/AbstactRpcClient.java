package com.netto.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.netto.client.provider.AbstractServiceProvider;
import com.netto.client.provider.ServiceProvider;
import com.netto.filter.Invocation;
import com.netto.filter.InvokeMethodFilter;

public abstract class AbstactRpcClient implements InvocationHandler {
	protected static Logger logger = Logger.getLogger(AbstactRpcClient.class);
	private final String serviceName;
	private int timeout = 10 * 1000;
	protected static Gson gson = new Gson();
	private List<InvokeMethodFilter> filters;
	private AbstractServiceProvider provider;

	public AbstactRpcClient(ServiceProvider provider, List<InvokeMethodFilter> filters, String serviceName,
			int timeout) {
		this.provider = (AbstractServiceProvider) provider;
		this.serviceName = serviceName;
		this.timeout = timeout;
		this.filters = filters;
	}

	protected AbstractServiceProvider getProvider() {
		return provider;
	}

	public String getServiceName() {
		return serviceName;
	}

	public int getTimeout() {
		return timeout;
	}

	private void invokeFiltersBefore(Invocation invocation) {
		if (this.filters == null) {
			return;
		}
		for (InvokeMethodFilter filter : filters) {
			filter.invokeBefore(invocation);
		}
	}

	private void invokeFiltersAfter(Invocation invocation) {
		if (this.filters == null) {
			return;
		}
		for (InvokeMethodFilter filter : filters) {
			filter.invokeAfter(invocation);
		}
	}

	private void invokeFiltersException(Invocation invocation, Throwable t) {
		if (this.filters == null) {
			return;
		}
		for (InvokeMethodFilter filter : filters) {
			filter.invokeException(invocation, t);
		}
	}

	protected abstract Object invokeMethod(Method method, Object[] args) throws Throwable;

	@Override
	public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Invocation invocation = new Invocation(this.serviceName, proxy, method, args);
		try {
			this.invokeFiltersBefore(invocation);
			return this.invokeMethod(method, args);
		} catch (Exception e) {
			this.invokeFiltersException(invocation, e);
			throw e;
		} finally {
			try {
				this.invokeFiltersAfter(invocation);
			} catch (Exception e) {
				;
			}
		}
	}

}
