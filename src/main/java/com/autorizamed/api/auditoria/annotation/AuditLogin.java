package com.autorizamed.api.auditoria.annotation;

import com.autorizamed.api.auditoria.enums.AcaoAuditoria;
import com.autorizamed.api.seguranca.entity.Usuario;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLogin {
    Class<?> entidadeTipo() default Usuario.class;
    AcaoAuditoria acao() default AcaoAuditoria.LOGIN;
}