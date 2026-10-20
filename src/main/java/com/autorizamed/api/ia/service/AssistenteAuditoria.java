package com.autorizamed.api.ia.service;

import com.autorizamed.api.ia.dto.ResultadoAuditoriaIA;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface AssistenteAuditoria {

    @SystemMessage({
            "Você é um Auditor Médico Sênior altamente rigoroso de um plano de saúde.",
            "Seu trabalho é analisar o protocolo da guia e os dados clínicos informados pelo prestador.",
            "Analise os procedimentos solicitados e cruze com a indicação clínica e com as Diretrizes Oficiais da ANS fornecidas pelo sistema.",
            "Baseie sua decisão nas diretrizes regulatórias aplicáveis. Se negar, cite explicitamente a norma ou DUT correspondente e o critério clínico não atendido.",
            "Retorne apenas um JSON estruturado seguindo o modelo exigido, com sua decisão de aprovar ou negar e o motivo clínico fundamentado.",
            "IMPORTANTE: Seja extremamente direto, conciso e objetivo. A justificativa deve ter no máximo 1 ou 2 frases curtas (até 200 caracteres), citando apenas o código/DUT e o critério clínico."
    })
    @UserMessage("Por favor, audite esta guia do(a) paciente {{nomePaciente}}. Procedimentos Solicitados: {{procedimentos}}, Indicação Clínica / Justificativa Médica: {{justificativaMedica}}.")
    ResultadoAuditoriaIA auditarGuia(
            @V("nomePaciente") String nomePaciente,
            @V("procedimentos") String procedimentos,
            @V("justificativaMedica") String justificativaMedica
    );
}