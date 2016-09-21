/*
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.dsl.support;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.Lifecycle;
import org.springframework.integration.handler.MessageProcessor;
import org.springframework.integration.handler.MethodInvokingMessageProcessor;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

/**
 * An "artificial" {@link MessageProcessor} for lazy-load of target bean by its name.
 * For internal use only.
 *
 * @param <T> the expected {@link #processMessage} result type.
 *
 * @author Artem Bilan
 */
public class BeanNameMessageProcessor<T> implements MessageProcessor<T>, BeanFactoryAware, Lifecycle {

	private final String beanName;

	private final String methodName;

	private final AtomicBoolean running = new AtomicBoolean();

	private MessageProcessor<T> delegate;

	private BeanFactory beanFactory;

	public BeanNameMessageProcessor(String object, String methodName) {
		this.beanName = object;
		this.methodName = methodName;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		Assert.notNull(beanFactory);
		this.beanFactory = beanFactory;
	}

	@Override
	public void start() {
		if (!this.running.getAndSet(true)) {
			Object target = this.beanFactory.getBean(this.beanName);
			this.delegate = new MethodInvokingMessageProcessor<T>(target, this.methodName);
		}
	}

	@Override
	public void stop() {
		this.running.set(false);
	}

	@Override
	public boolean isRunning() {
		return this.running.get();
	}

	@Override
	public T processMessage(Message<?> message) {
		return this.delegate.processMessage(message);
	}

}
