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
import com.spotify.docker.client.exceptions.DockerException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import static com.spotify.plugin.dockerfile.TagsSelector.select;

@Mojo(name = "remove",
    defaultPhase = LifecyclePhase.DEPLOY,
    requiresProject = true,
    threadSafe = true)
public class RemoveMojo extends AbstractDockerMojo {

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
   * Disables the remove goal; it becomes a no-op.
   */
  @Parameter(property = "dockerfile.remove.skip", defaultValue = "false")
  private boolean skipRemove;

  /**
   * Allow failure.
   */
  @Parameter(property = "dockerfile.remove.failure.ignore", defaultValue = "false")
  private boolean removeFailureIgnore;

  @Override
  protected void execute(DockerClient dockerClient)
          throws MojoExecutionException, MojoFailureException {
    if (removeFailureIgnore) {
      try {
        executeInternal(dockerClient);
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      executeInternal(dockerClient);
    }
  }

  protected void executeInternal(DockerClient dockerClient)
      throws MojoExecutionException, MojoFailureException {
    final Log log = getLog();

    if (skipRemove) {
      log.info("Skipping execution because 'dockerfile.remove.skip' is set");
      return;
    }

    if (repository == null) {
      repository = readMetadata(Metadata.REPOSITORY);
    }

    List<String> tagsToRemove = select(tags, tag, readMetadata(Metadata.TAG));

    if (repository == null) {
      throw new MojoExecutionException(
          "Can't remove image; image repository not known "
          + "(specify dockerfile.repository parameter, or run the tag goal before)");
    }

    for(String tagToRemove : tagsToRemove) {
      try {
        dockerClient
                .removeImage(formatImageName(repository, tagToRemove));
      }
      catch (DockerException | InterruptedException e) {
        throw new MojoExecutionException("Could not remove image", e);
      }
    }
  }
}
