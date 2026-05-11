package db;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class MensagemDAO {

    public static void inserirMensagensPadrao() throws Exception {
        inserir(1001, "Sistema iniciado.");
        inserir(1002, "Sistema encerrado.");
        inserir(1003, "Sessão iniciada para <login_name>.");
        inserir(1004, "Sessão encerrada para <login_name>.");
        inserir(1005, "Partida do sistema iniciada para cadastro do administrador.");
        inserir(1006, "Partida do sistema iniciada para operação normal pelos usuários.");

        inserir(2001, "Autenticação etapa 1 iniciada.");
        inserir(2002, "Autenticação etapa 1 encerrada.");
        inserir(2003, "Login name <login_name> identificado com acesso liberado.");
        inserir(2004, "Login name <login_name> identificado com acesso bloqueado.");
        inserir(2005, "Login name <login_name> não identificado.");

        inserir(3001, "Autenticação etapa 2 iniciada para <login_name>.");
        inserir(3002, "Autenticação etapa 2 encerrada para <login_name>.");
        inserir(3003, "Senha pessoal verificada positivamente para <login_name>.");
        inserir(3004, "Primeiro erro da senha pessoal contabilizado para <login_name>.");
        inserir(3005, "Segundo erro da senha pessoal contabilizado para <login_name>.");
        inserir(3006, "Terceiro erro da senha pessoal contabilizado para <login_name>.");
        inserir(3007, "Acesso do usuario <login_name> bloqueado pela autenticação etapa 2.");

        inserir(4001, "Autenticação etapa 3 iniciada para <login_name>.");
        inserir(4002, "Autenticação etapa 3 encerrada para <login_name>.");
        inserir(4003, "Token verificado positivamente para <login_name>.");
        inserir(4004, "Primeiro erro de token contabilizado para <login_name>.");
        inserir(4005, "Segundo erro de token contabilizado para <login_name>.");
        inserir(4006, "Terceiro erro de token contabilizado para <login_name>.");
        inserir(4007, "Acesso do usuario <login_name> bloqueado pela autenticação etapa 3.");

        inserir(5001, "Tela principal apresentada para <login_name>.");
        inserir(5002, "Opção 1 do menu principal selecionada por <login_name>.");
        inserir(5003, "Opção 2 do menu principal selecionada por <login_name>.");
        inserir(5004, "Opção 3 do menu principal selecionada por <login_name>.");

        inserir(6001, "Tela de cadastro apresentada para <login_name>.");
        inserir(6002, "Botão cadastrar pressionado por <login_name>.");
        inserir(6003, "Senha pessoal inválida fornecida por <login_name>.");
        inserir(6004, "Caminho do certificado digital inválido fornecido por <login_name>.");
        inserir(6005, "Chave privada verificada negativamente para <login_name> (caminho inválido).");
        inserir(6006, "Chave privada verificada negativamente para <login_name> (frase secreta inválida).");
        inserir(6007, "Chave privada verificada negativamente para <login_name> (assinatura digital inválida).");
        inserir(6008, "Confirmação de dados aceita por <login_name>.");
        inserir(6009, "Confirmação de dados rejeitada por <login_name>.");
        inserir(6010, "Botão voltar de cadastro para o menu principal pressionado por <login_name>.");

        inserir(7001, "Tela de consulta de arquivos secretos apresentada para <login_name>.");
        inserir(7002, "Botão voltar de consulta para o menu principal pressionado por <login_name>.");
        inserir(7003, "Botão Listar de consulta pressionado por <login_name>.");
        inserir(7004, "Caminho de pasta inválido fornecido por <login_name>.");
        inserir(7005, "Arquivo de índice decriptado com sucesso para <login_name>.");
        inserir(7006, "Arquivo de índice verificado (integridade e autenticidade) com sucesso para <login_name>.");
        inserir(7007, "Falha na decriptação do arquivo de índice para <login_name>.");
        inserir(7008, "Falha na verificação (integridade e autenticidade) do arquivo de índice para <login_name>.");
        inserir(7009, "Lista de arquivos presentes no índice apresentada para <login_name>.");
        inserir(7010, "Arquivo <arq_name> selecionado por <login_name> para decriptação.");
        inserir(7011, "Acesso permitido ao arquivo <arq_name> para <login_name>.");
        inserir(7012, "Acesso negado ao arquivo <arq_name> para <login_name>.");
        inserir(7013, "Arquivo <arq_name> decriptado com sucesso para <login_name>.");
        inserir(7014, "Arquivo <arq_name> verificado (integridade e autenticidade) com sucesso para <login_name>.");
        inserir(7015, "Falha na decriptação do arquivo <arq_name> para <login_name>.");
        inserir(7016, "Falha na verificação (integridade e autenticidade) do arquivo <arq_name> para <login_name>.");

        inserir(8001, "Tela de saída apresentada para <login_name>.");
        inserir(8002, "Botão encerrar sessão pressionado por <login_name>.");
        inserir(8003, "Botão encerrar sistema pressionado por <login_name>.");
        inserir(8004, "Botão voltar de sair para o menu principal pressionado por <login_name>.");
    }

    private static void inserir(int mid, String texto) throws Exception {
        String sql = "INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mid);
            ps.setString(2, texto);
            ps.executeUpdate();
        }
    }
}