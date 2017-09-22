package com.alfresco.aps.test.process;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.Deployment;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.ContextConfiguration;
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
public class APSRestStepProcessUnitTest extends AbstractTest {

	static // Process info.
	String appName = "Test App";
	static String appResourcePath = "app";
	String processDefinitionKey = "APSRestStepProcess";
	String bpmnFilePath = "src/main/resources/app/bpmn-models";

	@BeforeClass
	public static void beforeClass() throws Exception {
		ActivitiResources.force(appName, appResourcePath);
	}
	
	@Before
	public void before() throws Exception {

		Iterator it = FileUtils.iterateFiles(new File(bpmnFilePath), null, false);
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

		unitTestHelpers.waitForJobExecutorToProcessAllJobs(5000, 100);

		verify(activiti_restCallDelegate, times(1)).execute((DelegateExecution) any());

		unitTestHelpers.assertNullProcessInstance(processInstance.getProcessInstanceId());
	}

}