package com.autorizamed.api.auditoria.model;

import java.util.UUID;

public interface Auditavel {
    UUID getId();
    String getNomeEntidade();
}
