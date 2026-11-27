package com.comissions.korp.DTO;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Página simples, que vira JSON e volta sem perder nada (a Page do Spring não volta do JSON,
 * por isso não dá para guardá-la no cache do Redis).
 * Os nomes dos campos são os mesmos que o front já lê da Page: content, totalPages e totalElements.
 */
public record PaginaDTO<T>(
        List<T> content,
        int number,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PaginaDTO<T> de(Page<T> pagina) {
        return new PaginaDTO<>(
                pagina.getContent(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages()
        );
    }
}
