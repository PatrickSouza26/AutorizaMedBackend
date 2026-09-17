package com.autorizamed.api.auditoria.annotation;

import com.autorizamed.api.auditoria.enums.AcaoAuditoria;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditUpdate {
    Class<?> entidadeTipo();
    AcaoAuditoria acao() default AcaoAuditoria.ATUALIZACAO;
}