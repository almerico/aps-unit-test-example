package com.alfresco.aps.example.listeners;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ProcessEndExecutionListenerTest {
	
	@Configuration
    static class ContextConfiguration {
        @Bean
        public ProcessEndExecutionListener processEndExecutionListener() {
        	return new ProcessEndExecutionListener();
        }
    }
	
	@InjectMocks
	@Spy
	private static ProcessEndExecutionListener processEndExecutionListener;
	
	@Mock
	private DelegateExecution execution;
	
	@Before
	public void initMocks(){
		MockitoAnnotations.initMocks(this);	
	}
	
	@Test
	public void test() throws Exception {
		Map<String, Object> variableMap = new HashMap<String, Object>();
		
		doAnswer(new Answer<Void>() {
	        @Override
	        public Void answer(InvocationOnMock invocation) throws Throwable {
	        	Object[] arg = invocation.getArguments();
	        	variableMap.put((String) arg[0], arg[1]);	
	            return null;
	        }
	    }).when(execution).setVariable(anyString(), any());;
		
	    when(execution.getVariable(anyString())).thenAnswer(new Answer<String>() {
	        public String answer(InvocationOnMock invocation) {
	            return (String) variableMap.get(invocation.getArguments()[0]);
	        }
	    });
		processEndExecutionListener.notify(execution);
		//sample assertion to make sure that the java code is setting correct value
		assertThat(execution.getVariable("oddOrEven")).isNotNull().isIn("ODD", "EVEN");
	}

}
