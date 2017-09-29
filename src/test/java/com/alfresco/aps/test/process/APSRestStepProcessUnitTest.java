package com.alfresco.aps.test.process;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.repository.Deployment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alfresco.aps.testutils.AbstractTest;
import com.alfresco.aps.testutils.resources.ActivitiResources;

import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:activiti.cfg.xml", "classpath:common-beans-and-mocks.xml" })
@TestPropertySource(value="classpath:local-dev-test.properties")
public class APSRestStepProcessUnitTest extends AbstractTest {
	
	String appName = "Test App";
	String processDefinitionKey = "APSRestStepProcess";
	
	@Before
	public void before() throws Exception {
		
		if(env.getProperty ("aps.app.download", Boolean.class, false)){
			
			ActivitiResources.forceGet(appName);
		}
	
		Iterator<File> it = FileUtils.iterateFiles(new File(BPMN_RESOURCE_PATH), null, false);
		while (it.hasNext()) {
			String bpmnXml = ((File) it.next()).getPath();
			String extension = FilenameUtils.getExtension(bpmnXml);
			if (extension.equals("xml")) {
				activitiRule.getRepositoryService().createDeployment()
						.addInputStream(bpmnXml, new FileInputStream(bpmnXml)).deploy();
			}
		}

		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				
				Object[] arg = invocation.getArguments();
				DelegateExecution execution = (DelegateExecution) arg[0];
				HashMap<String, String> fieldExtensions = new HashMap<String, String>();
				fieldExtensions.put("restUrl", "https://api.github.com/");
				fieldExtensions.put("httpMethod", "GET");
				unitTestHelpers.assertFieldExtensions(2, execution, fieldExtensions);
				System.out.println("Process ID is " + execution.getProcessInstanceId());
				// mock as if the rest step sets a variable
				execution.setVariable("restResponse", "{}");
				return null;
			}
		}).when(activiti_restCallDelegate).execute((DelegateExecution) any());
	}

	@After
	public void after() {
		List<Deployment> deploymentList = activitiRule.getRepositoryService().createDeploymentQuery().list();
		for (Deployment deployment : deploymentList) {
			activitiRule.getRepositoryService().deleteDeployment(deployment.getId(), true);
		}
	}

	@Test
	public void testProcessExecution() throws Exception {

		ProcessInstance processInstance = activitiRule.getRuntimeService()
				.startProcessInstanceByKey(processDefinitionKey);

		assertNotNull(processInstance);

		//Wait for async step to execute
		unitTestHelpers.waitForJobExecutorToProcessAllJobs(5000, 100);

		verify(activiti_restCallDelegate, times(1)).execute((DelegateExecution) any());

		unitTestHelpers.assertNullProcessInstance(processInstance.getProcessInstanceId());
	}

}