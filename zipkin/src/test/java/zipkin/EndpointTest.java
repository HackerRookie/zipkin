/**
 * Copyright 2015-2016 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package zipkin;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class EndpointTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void messageWhenMissingServiceName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("serviceName");

    Endpoint.builder().ipv4(127 << 24 | 1).build();
  }

  @Test
  public void missingIpv4CoercesTo0() {
    assertThat(Endpoint.builder().serviceName("foo").build().ipv4)
        .isEqualTo(0);
  }

  @Test
  public void builderWithPort_0CoercesToNull() {
    assertThat(Endpoint.builder().serviceName("foo").port(0).build().port)
        .isNull();
  }

  @Test
  public void builderWithPort_highest() {
    short port = Endpoint.builder().serviceName("foo").port(65535).build().port;

    assertThat(port)
        .isEqualTo((short) -1); // an unsigned short of 65535 is the same as -1

    assertThat(port & 0xffff)
        .isEqualTo(65535);
  }

  /** The integer arg of port should be a whole number */
  @Test
  public void builderWithPort_negativeIsInvalid() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("invalid port -1");

    assertThat(Endpoint.builder().serviceName("foo").port(-1).build().port);
  }

  /** The integer arg of port should fit in a 16bit unsigned value */
  @Test
  public void builderWithPort_tooHighIsInvalid() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("invalid port 65536");

    assertThat(Endpoint.builder().serviceName("foo").port(65536).build().port);
  }

  @Test
  public void lowercasesServiceName() {
    assertThat(Endpoint.builder().serviceName("fFf").ipv4(127 << 24 | 1).build().serviceName)
        .isEqualTo("fff");
  }

  @Test
  public void testToStringIsJson_minimal() {
    assertThat(Endpoint.builder().serviceName("foo").build())
        .hasToString("{\"serviceName\":\"foo\"}");
  }

  @Test
  public void testToStringIsJson_ipv4() {
    assertThat(Endpoint.builder().serviceName("foo").ipv4(127 << 24 | 1).build())
        .hasToString("{\"serviceName\":\"foo\",\"ipv4\":\"127.0.0.1\"}");
  }

  @Test
  public void testToStringIsJson_ipv4Port() {
    assertThat(Endpoint.builder().serviceName("foo").ipv4(127 << 24 | 1).port(80).build())
        .hasToString("{\"serviceName\":\"foo\",\"ipv4\":\"127.0.0.1\",\"port\":80}");
  }

  @Test
  public void testToStringIsJson_ipv6() {
    assertThat(Endpoint.builder().serviceName("foo")
        // Cheat so we don't have to catch an exception here
        .ipv6(sun.net.util.IPAddressUtil.textToNumericFormatV6("2001:db8::c001")).build())
        .hasToString("{\"serviceName\":\"foo\",\"ipv6\":\"2001:db8::c001\"}");
  }
}
