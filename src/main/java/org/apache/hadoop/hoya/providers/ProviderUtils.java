/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hoya.providers;

import org.apache.hadoop.hoya.api.ClusterDescription;
import org.apache.hadoop.hoya.api.OptionKeys;
import org.apache.hadoop.hoya.api.RoleKeys;
import org.apache.hadoop.hoya.exceptions.BadCommandArgumentsException;
import org.apache.hadoop.hoya.exceptions.BadConfigException;
import org.apache.hadoop.hoya.tools.HoyaUtils;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Map;

/**
 * this is a factoring out of methods handy for providers. It's bonded to a log at
 * construction time
 */
public class ProviderUtils implements RoleKeys {

  protected final Logger log;

  public ProviderUtils(Logger log) {
    this.log = log;
  }

  /**
   * Validate requested JVM heap settings with the role options, and
   * flag if the JVM heap requested is larger than the 
   * @param role role with both YARN and heap settings
   * @return the JVM heap
   * @throws BadConfigException if the config is invalid
   */
  public int validateAndGetJavaHeapSettings(Map<String, String> role,
                                            int defHeap)
    throws BadConfigException {
    int yarnRAM = validateAndGetYARNMemory(role);
    return HoyaUtils.getIntValue(role, RoleKeys.JVM_HEAP, defHeap, 0, yarnRAM);
  }

  public int validateAndGetYARNMemory(Map<String, String> role) throws
                                                             BadConfigException {
    return HoyaUtils.getIntValue(role, RoleKeys.YARN_MEMORY, 0, 0, -1);
  }


  /**
   * build the log directory
   * @return the log dir
   */
  public String getLogdir() throws IOException {
    String logdir = System.getenv("LOGDIR");
    if (logdir == null) {
      logdir =
        "/tmp/hoya-" + UserGroupInformation.getCurrentUser().getShortUserName();
    }
    return logdir;
  }


  /**
   * Validate the node count and heap size values of a node class 
   *
   * @param name node class name
   * @param count requested node count
   * @param min requested heap size
   * @param max
   * @throws BadCommandArgumentsException if the values are out of range
   */
  public void validateNodeCount(String name,
                                int count,
                                int min,
                                int max) throws BadCommandArgumentsException {
    if (count < min) {
      throw new BadCommandArgumentsException(
        "requested no of %s nodes: %d is below the minimum of %d", name, count,
        min);
    }
    if (max > 0 && count > max) {
      throw new BadCommandArgumentsException(
        "requested no of %s nodes: %d is above the maximum of %d", name, count,
        max);
    }
  }

  /**
   * copy all options beginning site. into the site.xml
   * @param clusterSpec cluster specificatin
   * @param sitexml XML file to build up
   */
  public void propagateSiteOptions(ClusterDescription clusterSpec,
                                    Map<String, String> sitexml) {
    Map<String, String> options = clusterSpec.options;
    for (Map.Entry<String, String> entry : options.entrySet()) {
      String key = entry.getKey();
      if (key.startsWith(OptionKeys.OPTION_SITE_PREFIX)) {
        String envName = key.substring(OptionKeys.OPTION_SITE_PREFIX.length());
        if (!envName.isEmpty()) {
          sitexml.put(envName, entry.getValue());
        }
      }
    }
  }}
