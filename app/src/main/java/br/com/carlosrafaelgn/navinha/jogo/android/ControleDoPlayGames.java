package br.com.carlosrafaelgn.navinha.jogo.android;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;

import br.com.carlosrafaelgn.navinha.R;
import br.com.carlosrafaelgn.navinha.modelo.dados.persistencia.ArmazenamentoPersistente;
import br.com.carlosrafaelgn.navinha.modelo.jogo.Jogo;

public class ControleDoPlayGames implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	//----------------------------------------------------------------------------------------------
	// Constantes
	//----------------------------------------------------------------------------------------------

	private static final int TENTANDO_CONECTAR = 1000;

	private static final int MENSAGEM_CONECTE = 0x0100;
	private static final int MENSAGEM_DESCONECTE = 0x0101;
	private static final int MENSAGEM_EXIBA_CONQUISTAS = 0x0102;
	private static final int MENSAGEM_EXIBA_PLACAR = 0x0103;

	private static final int PLACAR_PENDENTE = 0;

	public static final int CONQUISTA_PRIMEIRA_VEZ = 0;
	public static final int CONQUISTA_PACIFISTA = 1;
	public static final int CONQUISTA_INVENCIVEL = 2;
	public static final int CONQUISTA_DESTRUIDOR = 3;
	public static final int CONQUISTA_ANIQUILADOR = 4;

	public static final int CONTAGEM_DE_CONQUISTAS = 5;

	// Não destruiu inimigos por 30 segundos
	public static final float TEMPO_DA_CONQUISTA_PACIFISTA = 30.0f;

	// Não levou tiros por 120 segundos
	public static final float TEMPO_DA_CONQUISTA_INVENCIVEL = 120.0f;

	//----------------------------------------------------------------------------------------------
	// Interfaces e classes internas
	//----------------------------------------------------------------------------------------------

	public interface Observador {
		//------------------------------------------------------------------------------------------
		// Métodos públicos
		//------------------------------------------------------------------------------------------

		void conexaoAlterada();
	}

	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private static final ControleDoPlayGames controleDoPlayGames = new ControleDoPlayGames();

	private Activity activity;
	private GoogleApiClient clienteGoogleApi;
	private Handler tratadorDaThreadPrincipal;
	private Observador observador;
	private String idDoPlacar;
	private String[] idsDasConquistas;
	private int placarPendente;
	private boolean primeiraTentativa, tentandoResolverConexao;
	private boolean[] conquistasDoJogoAtual, conquistasPendentes;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	private ControleDoPlayGames() {
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	public static ControleDoPlayGames getControleDoPlayGames() {
		return controleDoPlayGames;
	}

	private Activity getActivity() {
		return activity;
	}

	private void setActivity(Activity activity) {
		this.activity = activity;
	}

	private GoogleApiClient getClienteGoogleApi() {
		return clienteGoogleApi;
	}

	private void setClienteGoogleApi(GoogleApiClient clienteGoogleApi) {
		this.clienteGoogleApi = clienteGoogleApi;
	}

	private Handler getTratadorDaThreadPrincipal() {
		return tratadorDaThreadPrincipal;
	}

	private void setTratadorDaThreadPrincipal(Handler tratadorDaThreadPrincipal) {
		this.tratadorDaThreadPrincipal = tratadorDaThreadPrincipal;
	}

	public Observador getObservador() {
		return observador;
	}

	public void setObservador(Observador observador) {
		this.observador = observador;
	}

	private String getIdDoPlacar() {
		return idDoPlacar;
	}

	private void setIdDoPlacar(String idDoPlacar) {
		this.idDoPlacar = idDoPlacar;
	}

	private String[] getIdsDasConquistas() {
		return idsDasConquistas;
	}

	private void setIdsDasConquistas(String[] idsDasConquistas) {
		this.idsDasConquistas = idsDasConquistas;
	}

	private int getPlacarPendente() {
		return placarPendente;
	}

	private void setPlacarPendente(int placarPendente) {
		this.placarPendente = placarPendente;
	}

	private boolean isPrimeiraTentativa() {
		return primeiraTentativa;
	}

	private void setPrimeiraTentativa(boolean primeiraTentativa) {
		this.primeiraTentativa = primeiraTentativa;
	}

	private boolean isTentandoResolverConexao() {
		return tentandoResolverConexao;
	}

	private void setTentandoResolverConexao(boolean tentandoResolverConexao) {
		this.tentandoResolverConexao = tentandoResolverConexao;
	}

	private boolean[] getConquistasDoJogoAtual() {
		return conquistasDoJogoAtual;
	}

	private void setConquistasDoJogoAtual(boolean[] conquistasDoJogoAtual) {
		this.conquistasDoJogoAtual = conquistasDoJogoAtual;
	}

	private boolean[] getConquistasPendentes() {
		return conquistasPendentes;
	}

	private void setConquistasPendentes(boolean[] conquistasPendentes) {
		this.conquistasPendentes = conquistasPendentes;
	}

	public boolean isConectado() {
		// *** Muito, muito importante: isConectado() retornará true, mesmo que o dispositivo esteja
		// desconectado da Internet, desde que o jogador tenha se conectado com sucesso antes, e não
		// tenha clicado no botão para se desconectar
		GoogleApiClient clienteGoogleApi = getClienteGoogleApi();
		return (clienteGoogleApi != null && clienteGoogleApi.isConnected());
	}

	//----------------------------------------------------------------------------------------------
	// Métodos privados e protegidos
	//----------------------------------------------------------------------------------------------

	private void enviePendencias() {
		// Envia para o Play Games o placar e todas as conquistas que ficaram pendentes, e limpa
		// todas as pendências

		GoogleApiClient clienteGoogleApi = getClienteGoogleApi();

		if (clienteGoogleApi != null && clienteGoogleApi.isConnected()) {
			int placarPendente = getPlacarPendente();

			if (placarPendente > 0) {
				Games.Leaderboards.submitScore(clienteGoogleApi, getIdDoPlacar(), placarPendente);
				setPlacarPendente(0);
			}

			boolean[] conquistasPendentes = getConquistasPendentes();
			String[] idsDasConquistas = getIdsDasConquistas();

			for (int i = 0; i < CONTAGEM_DE_CONQUISTAS; i++) {
				if (conquistasPendentes[i]) {
					Games.Achievements.unlock(clienteGoogleApi, idsDasConquistas[i]);
					conquistasPendentes[i] = false;
				}
			}
		}
	}

	private void estadoDaConexaoAlterado() {
		// Avisa o observador (caso exista um) que o estado da conexão foi alterado
		Observador observador = getObservador();
		if (observador != null) {
			Jogo.getJogo().agendeParaOProximoQuadro(new Runnable() {
				@Override
				public void run() {
					Observador observador = getObservador();
					if (observador != null) {
						observador.conexaoAlterada();
					}
				}
			});
		}
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	public void prepare(Activity activity, View viewForPopups) {
		setActivity(activity);

		// Preenche os ids do placar e das conquistas do nosso jogo
		setIdDoPlacar(activity.getText(R.string.id_do_placar).toString());
		setIdsDasConquistas(new String[]{
			activity.getText(R.string.id_da_conquista_primeira_vez).toString(),
			activity.getText(R.string.id_da_conquista_pacifista).toString(),
			activity.getText(R.string.id_da_conquista_invencivel).toString(),
			activity.getText(R.string.id_da_conquista_destruidor).toString(),
			activity.getText(R.string.id_da_conquista_aniquilador).toString()
		});

		// Cria um cliente das APIs do Google, com acesso específico às funcionalidades de jogos
		// (Play Games)
		setClienteGoogleApi(new GoogleApiClient.Builder(activity)
			.addConnectionCallbacks(this)
			.addOnConnectionFailedListener(this)
			.addApi(Games.API, Games.GamesOptions.builder().setShowConnectingPopup(true).build())
			.addScope(Games.SCOPE_GAMES)
			.setGravityForPopups(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
			.setViewForPopups(viewForPopups)
			.build());

		// Esse tratador irá executar na thread principal todas as operações que forem pedidas por
		// outras threads
		setTratadorDaThreadPrincipal(new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				switch (msg.what) {
				case MENSAGEM_CONECTE:
					conecte();
					break;
				case MENSAGEM_DESCONECTE:
					desconecte();
					break;
				case MENSAGEM_EXIBA_CONQUISTAS:
					exibaConquistas();
					break;
				case MENSAGEM_EXIBA_PLACAR:
					exibaPlacar();
					break;
				}
				return true;
			}
		}));

		// Aqui serão armazenadas as conquistas já enviadas durante esse jogo
		setConquistasDoJogoAtual(new boolean[CONTAGEM_DE_CONQUISTAS]);

		// Recupera as pendências do último jogo
		ArmazenamentoPersistente armazenamentoPersistente = ArmazenamentoPersistente.carregueDoArquivo("playGames");
		if (armazenamentoPersistente == null) {
			armazenamentoPersistente = new ArmazenamentoPersistente();
		}
		setPlacarPendente(armazenamentoPersistente.getInt(PLACAR_PENDENTE));
		setConquistasPendentes(new boolean[] {
			armazenamentoPersistente.getBit(CONQUISTA_PRIMEIRA_VEZ),
			armazenamentoPersistente.getBit(CONQUISTA_PACIFISTA),
			armazenamentoPersistente.getBit(CONQUISTA_INVENCIVEL),
			armazenamentoPersistente.getBit(CONQUISTA_DESTRUIDOR),
			armazenamentoPersistente.getBit(CONQUISTA_ANIQUILADOR)
		});
		armazenamentoPersistente.destrua();

		// Da primeira vez que tentarmos nos conectar, vamos ignorar os erros, pois pode ser que o
		// jogador ainda não tenha efetivamente entrado no serviço (clicado no botão adequado na
		// tela de jogo)
		setPrimeiraTentativa(true);
		setTentandoResolverConexao(false);
		conecte();
	}

	public void conecte() {
		// Checagem básica (se não estamos na thread principal, envia uma mensagem para lá)
		if (Thread.currentThread() != Jogo.getJogo().getThreadPrincipal()) {
			Handler tratadorDaThreadPrincipal = getTratadorDaThreadPrincipal();
			if (tratadorDaThreadPrincipal != null) {
				tratadorDaThreadPrincipal.sendEmptyMessage(MENSAGEM_CONECTE);
			}
			return;
		}

		// Apesar da documentação do link:
		// https://developers.google.com/games/services/branding-guidelines#required-popups
		// pedir, até a versão 8.4.0 não existe uma forma para exibir o popup "Welcome Back" (que é
		// exibido automaticamente pela API, e, aparentemente, depende do intervalo de tempo, desde
		// a última vez que ele foi exibido
		if (isConectado()) {
			return;
		}

		GoogleApiClient clienteGoogleApi = getClienteGoogleApi();
		if (clienteGoogleApi != null) {
			clienteGoogleApi.connect();
		}
	}

	public void desconecte() {
		// Checagem básica (se não estamos na thread principal, envia uma mensagem para lá)
		if (Thread.currentThread() != Jogo.getJogo().getThreadPrincipal()) {
			Handler tratadorDaThreadPrincipal = getTratadorDaThreadPrincipal();
			if (tratadorDaThreadPrincipal != null) {
				tratadorDaThreadPrincipal.sendEmptyMessage(MENSAGEM_DESCONECTE);
			}
			return;
		}

		if (!isConectado()) {
			return;
		}

		// Apenas executar o método disconnect() não faz com que o jogador saia por completo, pois
		// para isso é preciso utilizar o método signOut()
		GoogleApiClient clienteGoogleApi = getClienteGoogleApi();
		if (clienteGoogleApi != null) {
			Games.signOut(clienteGoogleApi);
			clienteGoogleApi.disconnect();
		}
	}

	public void exibaConquistas() {
		// Checagem básica (se não estamos na thread principal, envia uma mensagem para lá)
		if (Thread.currentThread() != Jogo.getJogo().getThreadPrincipal()) {
			Handler tratadorDaThreadPrincipal = getTratadorDaThreadPrincipal();
			if (tratadorDaThreadPrincipal != null) {
				tratadorDaThreadPrincipal.sendEmptyMessage(MENSAGEM_EXIBA_CONQUISTAS);
			}
			return;
		}

		// Exibe todas as conquistas do jogador
		if (isConectado()) {
			getActivity().startActivityForResult(Games.Achievements.getAchievementsIntent(getClienteGoogleApi()), 0);
		}
	}

	public void exibaPlacar() {
		// Checagem básica (se não estamos na thread principal, envia uma mensagem para lá)
		if (Thread.currentThread() != Jogo.getJogo().getThreadPrincipal()) {
			Handler tratadorDaThreadPrincipal = getTratadorDaThreadPrincipal();
			if (tratadorDaThreadPrincipal != null) {
				tratadorDaThreadPrincipal.sendEmptyMessage(MENSAGEM_EXIBA_PLACAR);
			}
			return;
		}

		// Exibe o placar com as pontuações finais
		if (isConectado()) {
			getActivity().startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getClienteGoogleApi(), getIdDoPlacar()), 0);
		}
	}

	public void destrua() {
		// Checagem básica
		if (Thread.currentThread() != Jogo.getJogo().getThreadPrincipal()) {
			throw new RuntimeException("O método destrua() deve ser executado na thread principal");
		}

		// Apenas desconecta a API
		GoogleApiClient clienteGoogleApi = getClienteGoogleApi();
		if (clienteGoogleApi != null) {
			clienteGoogleApi.disconnect();
			setClienteGoogleApi(null);
		}

		// Grava as pendências em arquivo
		boolean[] conquistasPendentes = getConquistasPendentes();

		ArmazenamentoPersistente armazenamentoPersistente = new ArmazenamentoPersistente();

		armazenamentoPersistente.put(PLACAR_PENDENTE, getPlacarPendente());
		for (int i = 0; i < CONTAGEM_DE_CONQUISTAS; i++) {
			armazenamentoPersistente.putBit(i, conquistasPendentes[i]);
		}

		armazenamentoPersistente.graveEmArquivo("playGames");
		armazenamentoPersistente.destrua();

		setActivity(null);
		setIdsDasConquistas(null);
		setTratadorDaThreadPrincipal(null);
		setObservador(null);
	}

	public void atualizePlacar(int placar) {
		if (placar <= 0) {
			// Nada a fazer
			return;
		}

		GoogleApiClient clienteGoogleApi = getClienteGoogleApi();

		if (clienteGoogleApi != null && clienteGoogleApi.isConnected()) {
			// Se o placar que estava pendente era maior que o placar submetido, utiliza ele em vez
			// do placar submetido
			int placarPendente = getPlacarPendente();
			if (placar < placarPendente) {
				placar = placarPendente;
			}

			// Já atualiza o placar no Play Games
			Games.Leaderboards.submitScore(clienteGoogleApi, getIdDoPlacar(), placar);

			// O placar não está mais pendente
			setPlacarPendente(0);
		} else {
			// O placar agora está com seu envio pendente
			setPlacarPendente(placar);
		}
	}

	public void libereConquista(int conquista) {
		boolean[] conquistasDoJogoAtual = getConquistasDoJogoAtual();

		if (conquistasDoJogoAtual[conquista]) {
			// Essa conquista já foi liberada durante o jogo atual
			return;
		}

		// Marca que essa conquista já foi liberada durante o jogo atual (para evitar ficar chamando
		// o método Games.Achievements.unlock() constantemente)
		conquistasDoJogoAtual[conquista] = true;

		GoogleApiClient clienteGoogleApi = getClienteGoogleApi();

		if (clienteGoogleApi != null && clienteGoogleApi.isConnected()) {
			// Já envia a conquista para o Play Games
			Games.Achievements.unlock(clienteGoogleApi, getIdsDasConquistas()[conquista]);

			// Se essa conquista estava pendente, não está mais
			getConquistasPendentes()[conquista] = false;
		} else {
			// Essa conquista agora está com seu envio pendente
			getConquistasPendentes()[conquista] = true;
		}
	}

	public void reinicieConquistasDoJogoAtual() {
		// Provavelmente um novo jogo está se iniciando, e devemos reiniciar as conquistas

		boolean[] conquistasDoJogoAtual = getConquistasDoJogoAtual();

		for (int i = 0; i < CONTAGEM_DE_CONQUISTAS; i++) {
			conquistasDoJogoAtual[i] = false;
		}
	}

	public void onActivityResult(int requestCode, int resultCode) {
		if (requestCode == TENTANDO_CONECTAR) {
			setTentandoResolverConexao(false);
			if (resultCode == Activity.RESULT_OK) {
				// Vamos tentar nos conectar novamente, agora que o cliente do Google Play Games
				// parece ter resolvido o problema
				conecte();
			} else {
				// Algo saiu errado :(
				Activity activity = getActivity();
				if (activity != null) {
					String descricaoDoErro;

					switch (resultCode) {
					case GamesActivityResultCodes.RESULT_NETWORK_FAILURE:
						descricaoDoErro = "Não foi possível conectar-se à Internet :(";
						break;
					case GamesActivityResultCodes.RESULT_SIGN_IN_FAILED:
						descricaoDoErro = "Falha ao tentar efetuar o login :(";
						break;
					default:
						// Existem outros códigos de erros que poderiam ser tratados, mas vamos
						// ficar por aqui ;)
						descricaoDoErro = "Algo saiu errado na conexão com o Play Games :(";
						break;
					}

					Toast.makeText(activity, descricaoDoErro, Toast.LENGTH_LONG).show();
				}
			}
		}
	}

	@Override
	public void onConnected(Bundle bundle) {
		// Tudo saiu conforme o esperado, então apenas marca que já não estamos mais na primeira
		// tentativa
		setPrimeiraTentativa(false);

		// Se existiam conquistas com envio pendente, eis uma boa hora para enviá-las
		enviePendencias();

		estadoDaConexaoAlterado();
	}

	@Override
	public void onConnectionSuspended(int i) {
		// Se essa era a primeira tentativa, ignora
		if (isPrimeiraTentativa()) {
			setPrimeiraTentativa(false);
			return;
		}

		// Tenta recuperar a conexão, caso ela tenha sido perdida
		conecte();

		estadoDaConexaoAlterado();
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		// Se já estávamos tentando resolver o problema de conexão, ignora
		if (isTentandoResolverConexao()) {
			return;
		}

		// Se essa era a primeira tentativa, ignora
		if (isPrimeiraTentativa()) {
			setPrimeiraTentativa(false);
			return;
		}

		if (connectionResult.hasResolution()) {
			try {
				connectionResult.startResolutionForResult(getActivity(), TENTANDO_CONECTAR);
				setTentandoResolverConexao(true);
				return;
			} catch (Exception ex) {
				// Algo saiu errado :(
			}
		}

		// Alerta o jogador caso as coisas saiam errado
		Activity activity = getActivity();
		if (activity != null) {
			String erro = connectionResult.getErrorMessage();
			if (erro == null || erro.length() == 0) {
				erro = "Código " + connectionResult.getErrorCode();
			}
			Toast.makeText(activity, "Algo saiu errado na conexão com o Play Games: " + erro, Toast.LENGTH_LONG).show();
		}
	}
}
