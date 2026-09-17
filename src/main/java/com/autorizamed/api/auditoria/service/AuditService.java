package com.autorizamed.api.auditoria.service;

import com.autorizamed.api.auditoria.dto.AuditLogResponseDTO;
import com.autorizamed.api.auditoria.entity.AuditLog;
import com.autorizamed.api.auditoria.mapper.AuditLogMapper;
import com.autorizamed.api.auditoria.repository.AuditLogRepository;
import com.autorizamed.api.seguranca.entity.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void salvarLog(AuditLog log) {
        repository.save(log);
    }

    public AuditLog criarLogBase(String acao, Class<?> entidadeTipo) {
        AuditLog log = new AuditLog();
        log.setAcao(acao);
        log.setEntidadeTipo(entidadeTipo.getSimpleName());

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            log.setUsuarioIp(request.getRemoteAddr());
        } else {
            log.setUsuarioIp("IP_DESCONHECIDO");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Usuario usuarioLogado) {
            log.setUsuarioId(usuarioLogado.getId());
            log.setUsuarioNome(usuarioLogado.getNome());
        } else {
            log.setUsuarioNome("SISTEMA_OU_ANONIMO");
        }

        return log;
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponseDTO> buscarLogs(String pesquisa, String acao, String tipoEntidade, Pageable pageable) {
        return repository.buscarLogsComFiltros(pesquisa, acao, tipoEntidade, pageable)
                .map(AuditLogMapper::converteEntidade);
    }

    public String gerarHistoricoAlteracoes(Object objetoAntigo, Object objetoNovo) {
        if (objetoAntigo == null || objetoNovo == null) {
            return "Dados insuficientes para comparação.";
        }

        StringBuilder sb = new StringBuilder();
        try {
            Map<String, Object> mapaAntigo = objectMapper.convertValue(objetoAntigo, Map.class);
            Map<String, Object> mapaNovo = objectMapper.convertValue(objetoNovo, Map.class);

            for (String campo : mapaAntigo.keySet()) {
                Object valorAntigo = mapaAntigo.get(campo);
                Object valorNovo = mapaNovo.get(campo);

                if (valorNovo != null && !valorNovo.equals(valorAntigo)) {
                    if (!sb.isEmpty()) {
                        sb.append("; ");
                    }
                    sb.append(String.format("%s: '%s' -> '%s'", campo, valorAntigo, valorNovo));
                }
            }
        } catch (Exception e) {
            return "Erro ao mapear histórico de alterações.";
        }

        return sb.toString().isBlank() ? "Nenhuma alteração detectada." : sb.toString();
    }
}