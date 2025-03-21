/*
 * Copyright 2017-2024 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package example.micronaut

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.jwt.validator.ReactiveJsonWebTokenValidator
import io.micronaut.security.token.render.AccessRefreshToken
import io.micronaut.security.token.Claims;
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import reactor.core.publisher.Flux
import org.reactivestreams.Publisher
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import jakarta.inject.Inject

import static io.micronaut.http.HttpMethod.POST
import static io.micronaut.http.MediaType.APPLICATION_JSON_TYPE

@MicronautTest // <1>
class LoginLdapSpec extends Specification {

    @Inject
    @Client('/')
    HttpClient client // <2>

    @Shared
    @Inject
    ReactiveJsonWebTokenValidator tokenValidator // <3>

    void '/login with valid credentials returns 200 and access token and refresh token'() {
        when:
        HttpRequest request = HttpRequest.create(POST, '/login')
            .accept(APPLICATION_JSON_TYPE)
            .body(new UsernamePasswordCredentials('sherlock', 'elementary')) // <4>
        HttpResponse<AccessRefreshToken> rsp = client.toBlocking().exchange(request, AccessRefreshToken)

        then:
        rsp.status.code == 200
        rsp.body.present
        rsp.body.get().accessToken
    }

    @Ignore('TODO fix the timeout issue')
    void '/login with invalid credentials returns UNAUTHORIZED'() {
        when:
        HttpRequest request = HttpRequest.create(POST, '/login')
            .accept(APPLICATION_JSON_TYPE)
            .body(new UsernamePasswordCredentials('euler', 'bogus')) // <4>
        client.toBlocking().exchange(request)

        then:
        HttpClientResponseException e = thrown()
        e.status.code == 401 // <5>
    }

    void 'access token contains expiration date'() {
        when:
        HttpRequest request = HttpRequest.create(POST, '/login')
            .accept(APPLICATION_JSON_TYPE)
            .body(new UsernamePasswordCredentials('sherlock', 'elementary')) // <4>
        HttpResponse<AccessRefreshToken> rsp = client.toBlocking().exchange(request, AccessRefreshToken)

        then:
        rsp.status.code == 200
        rsp.body.present

        when:
        String accessToken = rsp.body.get().accessToken

        then:
        accessToken

        when:
        Publisher authentication = tokenValidator.validateToken(accessToken, request) // <6>

        then:
        Flux.from(authentication).blockFirst()

        and: 'access token contains an expiration date'
        Flux.from(authentication).blockFirst().attributes.get(Claims.EXPIRATION_TIME)
    }
}
