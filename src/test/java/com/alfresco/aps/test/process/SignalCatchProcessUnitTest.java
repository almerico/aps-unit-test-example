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
public class SignalCatchProcessUnitTest extends AbstractTest {

	static // Process info.
	String appName = "Test App";
	static String appResourcePath = "app";
	String processDefinitionKey = "SignalCatch";
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


		//Assert signal and not execute
		unitTestHelpers.assertSignalWait("signal-catch", false, null);
		//Assert boundary signal and not execute
		unitTestHelpers.assertSignalWait("signal-boundary", false, null);
		
		//Assert signal and execute
		unitTestHelpers.assertSignalWait("signal-catch", true, null);
		//Assert boundary signal and execute
		unitTestHelpers.assertSignalWait("signal-boundary", true, null);

		unitTestHelpers.assertNullProcessInstance(processInstance.getProcessInstanceId());
	}

}