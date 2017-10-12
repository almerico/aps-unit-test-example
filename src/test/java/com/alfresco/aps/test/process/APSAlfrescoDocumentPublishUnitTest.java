package com.alfresco.aps.test.process;

import java.util.HashMap;
import org.activiti.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alfresco.aps.testutils.AbstractBpmnTest;
import com.alfresco.aps.testutils.assertions.DelegateExecutionAssert;
import com.alfresco.aps.testutils.assertions.ProcessInstanceAssert;

import org.activiti.engine.runtime.ProcessInstance;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:activiti.cfg.xml", "classpath:common-beans-and-mocks.xml" })
public class APSAlfrescoDocumentPublishUnitTest extends AbstractBpmnTest {
	
	static {
		appName = "Test App";
		processDefinitionKey = "APSAlfrescoDocumentPublish";
	}
	
	@Test
	public void testProcessExecution() throws Exception {
		
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				
				Object[] arg = invocation.getArguments();
				DelegateExecution execution = (DelegateExecution) arg[0];
				HashMap<String, String> fieldExtensions = new HashMap<String, String>();
				fieldExtensions.put("contentSource", "process");
				fieldExtensions.put("account", "alfresco-2002-alfresco-2");
				fieldExtensions.put("site", "my-site");
				fieldExtensions.put("publishAsType", "process_initiator");
				DelegateExecutionAssert.assertThat(execution).assertFieldExtensions(5, fieldExtensions);
				return null;
			}
		}).when(activiti_publishAlfrescoDelegate).execute((DelegateExecution) any());

		ProcessInstance processInstance = activitiRule.getRuntimeService()
				.startProcessInstanceByKey(processDefinitionKey);

		assertNotNull(processInstance);

		//Wait for async step to execute
		unitTestHelpers.waitForJobExecutorToProcessAllJobs(5000, 100);

		verify(activiti_publishAlfrescoDelegate, times(1)).execute((DelegateExecution) any());

		ProcessInstanceAssert.assertThat(processInstance).isComplete();
	}

}