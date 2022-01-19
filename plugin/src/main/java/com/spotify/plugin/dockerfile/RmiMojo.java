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

import java.util.List;

import com.spotify.docker.client.DockerClient;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import static com.spotify.plugin.dockerfile.TagsSelector.select;

@Mojo(name = "rmi",
    defaultPhase = LifecyclePhase.DEPLOY,
    requiresProject = true,
    threadSafe = true)
public class RmiMojo extends AbstractDockerMojo {

  /**
   * The repository to put the built image into, for example <tt>spotify/foo</tt>.  You should also
   * set the <tt>tag</tt> parameter, otherwise the tag <tt>latest</tt> is used by default.
   */
  @Parameter(property = "dockerfile.repository")
  private String repository;

  /**
   * The tag to apply to the built image.
   */
  @Parameter(property = "dockerfile.tag")
  private String tag;

  @Parameter(property = "dockerfile.tags")
  private List<String> tags;

  /**
   * Disables the rmi goal; it becomes a no-op.
   */
  @Parameter(property = "dockerfile.rmi.skip", defaultValue = "false")
  private boolean skipRmi;

  @Override
  protected void execute(DockerClient dockerClient)
      throws MojoExecutionException, MojoFailureException {
    final Log log = getLog();

    if (skipRmi) {
      log.info("Skipping execution because 'dockerfile.rmi.skip' is set");
      return;
    }

    List<String> tagsToRmi = select(tags, tag, readMetadata(Metadata.TAG));

    for(String tagToRmi : tagsToRmi) {
      String imageName = formatImageName(repository, tagToRmi);
      try {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(
          "docker",
          "rmi",
          imageName
        );
        Process process = processBuilder.start();
        int ret = process.waitFor();
        System.out.printf("Program exited with code: %d", ret);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
