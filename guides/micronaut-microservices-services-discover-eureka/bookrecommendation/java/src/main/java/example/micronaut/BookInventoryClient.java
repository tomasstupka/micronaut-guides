//tag::packageandimports[]
package example.micronaut;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import io.micronaut.retry.annotation.Recoverable;
import javax.validation.constraints.NotBlank;
//end::packageandimports[]

/*
//tag::harcoded[]
@Client("http://localhost:8082") // <1>
@Recoverable(api = BookInventoryOperations.class)
//end::harcoded[]
*/
//tag::eureka[]
@Client(id = "bookinventory") // <1>
@Recoverable(api = BookInventoryOperations.class)
//end::eureka[]
//tag::clazz[]
interface BookInventoryClient extends BookInventoryOperations {

    @Consumes(MediaType.TEXT_PLAIN)
    @Get("/books/stock/{isbn}")
    Mono<Boolean> stock(@NonNull @NotBlank String isbn);
}
//end::clazz[]
