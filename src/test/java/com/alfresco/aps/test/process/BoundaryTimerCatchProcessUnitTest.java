package com.alfresco.aps.test.process;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;

import org.activiti.engine.repository.Deployment;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alfresco.aps.testutils.AbstractTest;
import com.alfresco.aps.testutils.resources.ActivitiResources;

import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:activiti.cfg.xml", "classpath:common-beans-and-mocks.xml" })
public class BoundaryTimerCatchProcessUnitTest extends AbstractTest {

	static // Process info.
	String appName = "Test App";
	static String appResourcePath = "app";
	String processDefinitionKey = "BoundaryTimerCatchProcess";
	String bpmnFilePath = "src/main/resources/app/bpmn-models";

	@BeforeClass
	public static void beforeClass() throws Exception {
		ActivitiResources.force(appName, appResourcePath);
	}
	
	@Before
	public void before() throws Exception {

		Iterator<File> it = FileUtils.iterateFiles(new File(bpmnFilePath), null, false);
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


		//Assert in seconds and execute/action timer
		unitTestHelpers.assertTimerJobsTimeInSecondsLowerThan(300, true);
		//Assert days and execute/action timer
		unitTestHelpers.assertTimerJobDateLowerThan(1, true);

		unitTestHelpers.assertNullProcessInstance(processInstance.getProcessInstanceId());
	}

}