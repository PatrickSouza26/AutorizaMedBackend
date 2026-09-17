package com.autorizamed.api.auditoria.aspect;

import com.autorizamed.api.auditoria.annotation.*;
import com.autorizamed.api.auditoria.entity.AuditLog;
import com.autorizamed.api.auditoria.model.Auditavel;
import com.autorizamed.api.auditoria.service.AuditService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    //ESCUTA CRIAÇÕES
    @Around("@annotation(auditAnnotation)")
    public Object auditCreateAction(ProceedingJoinPoint jp, AuditCreate auditAnnotation) throws Throwable {
        Object result = jp.proceed();

        try {
            AuditLog log = auditService.criarLogBase(auditAnnotation.acao().name(), auditAnnotation.entidadeTipo());

            if (result instanceof Auditavel auditavel) {
                log.setEntidadeId(auditavel.getId());
                log.setEntidadeNome(auditavel.getNomeEntidade());
                log.setDescricao(objectMapper.writeValueAsString(result));
            }

            auditService.salvarLog(log);
        } catch (Exception e) {
            System.err.println("Erro ao salvar log de criação: " + e.getMessage());
        }

        return result;
    }

    //ESCUTA QUALQUER ATUALIZAÇÃO
    @Around("@annotation(auditAnnotation)")
    public Object auditUpdateAction(ProceedingJoinPoint jp, AuditUpdate auditAnnotation) throws Throwable {
        Object[] parametros = jp.getArgs();
        Object id = obterId(parametros);
        Object estadoAntigo = null;

        if (id != null) {
            estadoAntigo = entityManager.find(auditAnnotation.entidadeTipo(), id);
            if (estadoAntigo != null) {
                entityManager.detach(estadoAntigo);
            }
        }

        Object result = jp.proceed();

        try {
            AuditLog log = auditService.criarLogBase(auditAnnotation.acao().name(), auditAnnotation.entidadeTipo());

            if (result instanceof Auditavel auditavel) {
                log.setEntidadeId(auditavel.getId());
                log.setEntidadeNome(auditavel.getNomeEntidade());
            }

            if (estadoAntigo != null && result != null) {
                String diferencas = auditService.gerarHistoricoAlteracoes(estadoAntigo, result);
                if ("Nenhuma alteração detectada.".equals(diferencas)) {
                    return result;
                }
                log.setDescricao(diferencas);
            }

            auditService.salvarLog(log);
        } catch (Exception e) {
            System.err.println("Erro ao salvar log de atualização: " + e.getMessage());
        }

        return result;
    }

    //ESCUTA LOGINS
    @Around("@annotation(auditAnnotation)")
    public Object auditLoginAction(ProceedingJoinPoint jp, AuditLogin auditAnnotation) throws Throwable {
        Object result = jp.proceed();

        try {
            AuditLog log = auditService.criarLogBase(auditAnnotation.acao().name(), auditAnnotation.entidadeTipo());
            
            log.setEntidadeId(log.getUsuarioId());
            log.setEntidadeNome(log.getUsuarioNome());
            log.setDescricao("Autenticação realizada com sucesso.");

            auditService.salvarLog(log);
        } catch (Exception e) {
            System.err.println("Erro ao salvar log de login: " + e.getMessage());
        }

        return result;
    }

    private Object obterId(Object[] parametros) {
        for (Object parametro : parametros) {
            if (parametro instanceof UUID) {
                return parametro;
            }
        }
        return null;
    }
}