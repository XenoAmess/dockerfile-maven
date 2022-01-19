/*-
 * -\-\-
 * Dockerfile Maven Plugin
 * --
 * Copyright (C) 2016 - 2020 Spotify AB
 * --
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
 * -/-/-
 */

package com.spotify.plugin.dockerfile;

import com.spotify.docker.client.DockerClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "exec",
    defaultPhase = LifecyclePhase.DEPLOY,
    requiresProject = true,
    threadSafe = true)
public class ExecMojo extends AbstractDockerMojo {

  /**
   * Disables the exec goal; it becomes a no-op.
   */
  @Parameter(property = "dockerfile.exec.skip", defaultValue = "false")
  private boolean skipExec;

  /**
   * Disables the build goal; it becomes a no-op.
   */
  @Parameter(property = "dockerfile.exec.command", defaultValue = "")
  private String execCommand;

  @Override
  protected void execute(DockerClient dockerClient)
      throws MojoExecutionException, MojoFailureException {
    final Log log = getLog();

    if (skipExec) {
      log.info("Skipping execution because 'dockerfile.exec.skip' is set");
      return;
    }
    if (StringUtils.isBlank(execCommand)) {
      log.info("Skipping execution because 'dockerfile.exec.command' is blank");
      return;
    }
    try {
      ProcessBuilder processBuilder = new ProcessBuilder();
      processBuilder.command(StringUtils.split(execCommand, ' '));
      Process process = processBuilder.start();
      int ret = process.waitFor();
      System.out.printf("Program exited with code: %d", ret);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
