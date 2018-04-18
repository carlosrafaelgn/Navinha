//
// Navinha is distributed under the FreeBSD License
//
// Copyright (c) 2016, Carlos Rafael Gimenes das Neves
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this
//    list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright notice,
//    this list of conditions and the following disclaimer in the documentation
//    and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
// ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// The views and conclusions contained in the software and documentation are those
// of the authors and should not be interpreted as representing official policies,
// either expressed or implied, of the FreeBSD Project.
//
// https://github.com/carlosrafaelgn/Navinha
//
package br.com.carlosrafaelgn.navinha.jogo.cenarios;

import br.com.carlosrafaelgn.navinha.R;
import br.com.carlosrafaelgn.navinha.jogo.android.ControleDoPlayGames;
import br.com.carlosrafaelgn.navinha.jogo.desenho.FolhaDeSprites;
import br.com.carlosrafaelgn.navinha.jogo.desenho.Vista;
import br.com.carlosrafaelgn.navinha.jogo.elementos.CampoEstelar;
import br.com.carlosrafaelgn.navinha.jogo.elementos.ElementoDeTelaComPausa;
import br.com.carlosrafaelgn.navinha.jogo.elementos.Fade;
import br.com.carlosrafaelgn.navinha.jogo.elementos.FadeEstatico;
import br.com.carlosrafaelgn.navinha.jogo.elementos.HordaDeInimigos;
import br.com.carlosrafaelgn.navinha.jogo.elementos.Inimigo;
import br.com.carlosrafaelgn.navinha.jogo.elementos.Nave;
import br.com.carlosrafaelgn.navinha.jogo.persistencia.Persistencia;
import br.com.carlosrafaelgn.navinha.modelo.desenho.AlinhamentoDoPivo;
import br.com.carlosrafaelgn.navinha.modelo.interacao.BotaoVirtual;
import br.com.carlosrafaelgn.navinha.modelo.jogo.Cenario;
import br.com.carlosrafaelgn.navinha.modelo.jogo.Jogo;
import br.com.carlosrafaelgn.navinha.modelo.interacao.Ponteiro;
import br.com.carlosrafaelgn.navinha.modelo.animacao.interpoladores.Interpolador;
import br.com.carlosrafaelgn.navinha.modelo.animacao.interpoladores.InterpoladorDePontos;
import br.com.carlosrafaelgn.navinha.modelo.dados.imutavel.VetorFloat;
import br.com.carlosrafaelgn.navinha.modelo.desenho.Tela;
import br.com.carlosrafaelgn.navinha.modelo.elemento.ListaDeElementosDeTela;
import br.com.carlosrafaelgn.navinha.modelo.recurso.ArmazenamentoDeRecursos;
import br.com.carlosrafaelgn.navinha.modelo.texto.Alfabeto;
import br.com.carlosrafaelgn.navinha.modelo.texto.TextoAnimado;
import br.com.carlosrafaelgn.navinha.modelo.texto.TextoEstatico;

public class CenarioJogo extends Cenario implements Nave.Observador, Inimigo.Observador {
	//----------------------------------------------------------------------------------------------
	// Constantes
	//----------------------------------------------------------------------------------------------

	private static final int COR_DOS_TEXTOS = 0xff4499ff;

	private static final float DURACAO_DO_FADE = 1.0f;
	private static final float DURACAO_DO_FADE_DA_EXPLICACAO = 0.5f;
	private static final float DURACAO_DO_GAME_OVER = 2.0f;

	private static final float OPACIDADE_DO_FUNDO = 0.70f;

	//----------------------------------------------------------------------------------------------
	// Interfaces e classes internas
	//----------------------------------------------------------------------------------------------

	private interface ProcessadorDeEstados {
		//------------------------------------------------------------------------------------------
		// Métodos públicos
		//------------------------------------------------------------------------------------------

		void perdeuInteratividadeComJogador();
		void botaoVoltarPressionado();
		void posProcessamentoDoQuadro(float deltaSegundos);
		void destrua();
	}

	private class ProcessadorDeEntrada implements ProcessadorDeEstados, Fade.Observador {
		//------------------------------------------------------------------------------------------
		// Campos privados
		//------------------------------------------------------------------------------------------

		private TextoAnimado textoExplicacao;

		//------------------------------------------------------------------------------------------
		// Construtores
		//------------------------------------------------------------------------------------------

		public ProcessadorDeEntrada() {
			ListaDeElementosDeTela listaDeElementosDeTela = getListaDeElementosDeTela();

			if (!isIgnorandoExplicacao()) {
				// Mostra uma breve explicação sobre o jogo, caso não seja pedido para ignorar

				Jogo jogo = Jogo.getJogo();
				Tela tela = Tela.getTela();

				String texto;

				if (getNave().isControladaPorMovimento()) {
					texto = jogo.texto(R.string.explicacao) + "\n\n" + jogo.texto(R.string.explicacao_controle_por_movimento) + "\n\n" + jogo.texto(R.string.explicacao_final);
				} else {
					texto = jogo.texto(R.string.explicacao) + "\n\n" + jogo.texto(R.string.explicacao_controle_por_toque) + "\n\n" + jogo.texto(R.string.explicacao_final);
				}

				TextoAnimado textoExplicacao = new TextoAnimado(getAlfabeto(),
					texto,
					0.5f * getTamanhoDoTexto(),
					AlinhamentoDoPivo.HORIZONTAL_CENTRO | AlinhamentoDoPivo.VERTICAL_CENTRO,
					DURACAO_DO_FADE_DA_EXPLICACAO,
					InterpoladorDePontos.crieConstante(
						0.5f * tela.getLarguraDaVista(),
						0.5f * tela.getAlturaDaVista()
					));
				textoExplicacao.setAlinhamentoHorizontalDasLinhas(AlinhamentoDoPivo.HORIZONTAL_CENTRO);
				setTextoExplicacao(textoExplicacao);

				listaDeElementosDeTela.adicioneAoInicio(textoExplicacao);
			}

			// Cria o fade de entrada
			Fade fade = new Fade(getFolhaDeSprites(), DURACAO_DO_FADE, true);
			fade.setObservador(this);
			listaDeElementosDeTela.adicioneAoInicio(fade);
		}

		//------------------------------------------------------------------------------------------
		// Métodos acessores e modificadores
		//------------------------------------------------------------------------------------------

		private TextoAnimado getTextoExplicacao() {
			return textoExplicacao;
		}

		private void setTextoExplicacao(TextoAnimado textoExplicacao) {
			this.textoExplicacao = textoExplicacao;
		}

		//------------------------------------------------------------------------------------------
		// Métodos públicos
		//------------------------------------------------------------------------------------------

		@Override
		public void animacaoDoFadeTerminada(Fade fade) {
			// Não precisaremos mais do fade
			fade.removaEDestrua();

			// Espera o jogador clicar/tocar na tela para continuar, ou vai direto para o jogo,
			// caso tenha sido pedido para ignorar a explicação
			setProcessadorDeEstados(isIgnorandoExplicacao() ?
				new ProcessadorDoJogo(true) :
				new ProcessadorDePreJogo(getTextoExplicacao())
			);
		}

		@Override
		public void perdeuInteratividadeComJogador() {
			// Nada a fazer durante a entrada
		}

		@Override
		public void botaoVoltarPressionado() {
			// Apenas ignora o botão voltar durante a entrada
		}

		@Override
		public void posProcessamentoDoQuadro(float deltaSegundos) {
			// Nada o que processar durante a entrada
		}

		@Override
		public void destrua() {
			// Vamos invalidar o objeto

			setTextoExplicacao(null);
		}
	}

	private class ProcessadorDePreJogo implements ProcessadorDeEstados, TextoAnimado.Observador {
		//------------------------------------------------------------------------------------------
		// Campos privados
		//------------------------------------------------------------------------------------------

		private TextoAnimado textoExplicacao;

		//------------------------------------------------------------------------------------------
		// Construtores
		//------------------------------------------------------------------------------------------

		public ProcessadorDePreJogo(TextoAnimado textoExplicacao) {
			setTextoExplicacao(textoExplicacao);
		}

		//------------------------------------------------------------------------------------------
		// Métodos acessores e modificadores
		//------------------------------------------------------------------------------------------

		private TextoAnimado getTextoExplicacao() {
			return textoExplicacao;
		}

		private void setTextoExplicacao(TextoAnimado textoExplicacao) {
			this.textoExplicacao = textoExplicacao;
		}

		//------------------------------------------------------------------------------------------
		// Métodos públicos
		//------------------------------------------------------------------------------------------

		@Override
		public void animacaoDoTextoTerminada(TextoAnimado textoAnimado) {
			// Não precisaremos mais do texto
			textoAnimado.removaEDestrua();

			// Passa para o estado do jogo
			setProcessadorDeEstados(new ProcessadorDoJogo(true));
		}

		@Override
		public void perdeuInteratividadeComJogador() {
			// Nada a fazer durante a explicação
		}

		@Override
		public void botaoVoltarPressionado() {
			// Apenas ignora o botão voltar durante a explicação
		}

		@Override
		public void posProcessamentoDoQuadro(float deltaSegundos) {
			// Espera o jogador clicar/tocar na tela para continuar

			if (Jogo.getJogo().getPonteiroPrincipal().isToqueRecemTerminado()) {
				TextoAnimado textoExplicacao = getTextoExplicacao();
				textoExplicacao.setAnimacaoInvertida(true);
				textoExplicacao.setInterpoladorDoAlpha(Interpolador.crieAceleradoDesacelerado());
				textoExplicacao.reinicieAnimacao(DURACAO_DO_FADE_DA_EXPLICACAO);
				textoExplicacao.setObservador(this);
			}
		}

		@Override
		public void destrua() {
			// Vamos invalidar o objeto

			setTextoExplicacao(null);
		}
	}

	private class ProcessadorDoJogo implements ProcessadorDeEstados {
		//------------------------------------------------------------------------------------------
		// Construtores
		//------------------------------------------------------------------------------------------

		public ProcessadorDoJogo(boolean primeiraVez) {
			// Vamos permitir que todos os elementos voltem a funcionar normalmente
			ElementoDeTelaComPausa.setPausado(false);

			if (primeiraVez) {
				// Para fazer com que o jogo comece a valer, basta habilitar o controle da nave, exibir
				// a mensagem de Pause na tela e criar a primeira horda de inimigos
				getNave().setHabilitada(true);

				// São tantos inimigos que é melhor deixar outra classe controlá-los
				crieNovaHordaDeInimigos();
			}

			// Mostra o botão de pausa
			getListaDeElementosDeTela().adicioneAoInicio(getTextoPausar());
		}

		//------------------------------------------------------------------------------------------
		// Métodos públicos
		//------------------------------------------------------------------------------------------

		@Override
		public void perdeuInteratividadeComJogador() {
			// Se perdemos a interação com o jogador durante o jogo, pausamos o jogo, para que ele
			// não seja pego de surpresa e tenha algum tempo para pensar, quando voltar ao jogo
			setProcessadorDeEstados(new ProcessadorDoJogoPausado());
		}

		@Override
		public void botaoVoltarPressionado() {
			// Pausa o jogo
			setProcessadorDeEstados(new ProcessadorDoJogoPausado());
		}

		@Override
		public void posProcessamentoDoQuadro(float deltaSegundos) {
			// Move a horda de inimigos pela tela
			getHordaDeInimigos().processeUmQuadro(deltaSegundos);

			// Verifica as duas conquistas associadas ao tempo
			if (!isConquistaPacifistaLiberada()) {
				float tempoTotalConquistaPacifista = getTempoTotalConquistaPacifista() + deltaSegundos;
				if (tempoTotalConquistaPacifista >= ControleDoPlayGames.TEMPO_DA_CONQUISTA_PACIFISTA) {
					ControleDoPlayGames.getControleDoPlayGames().libereConquista(ControleDoPlayGames.CONQUISTA_PACIFISTA);
					// Vamos marcar que a conquista já foi liberada para economizar processamento
					setConquistaPacifistaLiberada(true);
				}
				setTempoTotalConquistaPacifista(tempoTotalConquistaPacifista);
			}

			if (!isConquistaInvencivelLiberada()) {
				float tempoTotalConquistaInvencivel = getTempoTotalConquistaInvencivel() + deltaSegundos;
				if (tempoTotalConquistaInvencivel >= ControleDoPlayGames.TEMPO_DA_CONQUISTA_INVENCIVEL) {
					ControleDoPlayGames.getControleDoPlayGames().libereConquista(ControleDoPlayGames.CONQUISTA_INVENCIVEL);
					// Vamos marcar que a conquista já foi liberada para economizar processamento
					setConquistaInvencivelLiberada(true);
				}
				setTempoTotalConquistaInvencivel(tempoTotalConquistaInvencivel);
			}

			// Verifica se o jogador tocou/clicou na tela para pausar
			Ponteiro ponteiro = Jogo.getJogo().getPonteiroPrincipal();

			if (ponteiro.isToqueRecemTerminado()) {
				// Se clicar/tocar no botão pausar, pausa do jogo
				if (getBotaoPausar().contemPonteiro(ponteiro)) {
					setProcessadorDeEstados(new ProcessadorDoJogoPausado());
				}
			}
		}

		@Override
		public void destrua() {
			// Nada a fazer
		}
	}

	private class ProcessadorDoJogoPausado implements ProcessadorDeEstados {
		//------------------------------------------------------------------------------------------
		// Campos privados
		//------------------------------------------------------------------------------------------

		private FadeEstatico fundo;
		private TextoEstatico textoPausado, textoSair, textoControle;
		private BotaoVirtual botaoSair, botaoControle;

		//------------------------------------------------------------------------------------------
		// Construtores
		//------------------------------------------------------------------------------------------

		public ProcessadorDoJogoPausado() {
			// Para o processamento dos elementos
			ElementoDeTelaComPausa.setPausado(true);

			Jogo jogo = Jogo.getJogo();
			Tela tela = Tela.getTela();
			Alfabeto alfabeto = getAlfabeto();

			float margemDosBotoes = getMargemDosBotoes();
			float tamanhoDoTexto = getTamanhoDoTexto();
			float larguraDaVista = tela.getLarguraDaVista();
			float alturaDaVista = tela.getAlturaDaVista();

			// Configura o layout da tela de pausa
			ListaDeElementosDeTela listaDeElementosDeTela = getListaDeElementosDeTela();

			// Cria o fundo translúcido que será utilizado como fundo da tela de pausa
			FadeEstatico fundo = new FadeEstatico(getFolhaDeSprites(), OPACIDADE_DO_FUNDO);
			setFundo(fundo);

			// Cria os textos e botões que serão utilizados na tela de pausa
			TextoEstatico textoPausado = new TextoEstatico(alfabeto,
				jogo.texto(R.string.pausado),
				0.0f,
				AlinhamentoDoPivo.HORIZONTAL_CENTRO | AlinhamentoDoPivo.VERTICAL_CIMA,
				0.5f * larguraDaVista,
				0.5f * alturaDaVista);
			setTextoPausado(textoPausado);

			TextoEstatico textoSair = new TextoEstatico(alfabeto,
				jogo.texto(R.string.sair),
				0.0f,
				AlinhamentoDoPivo.HORIZONTAL_DIREITO | AlinhamentoDoPivo.VERTICAL_CIMA,
				larguraDaVista - tamanhoDoTexto,
				tamanhoDoTexto);
			setTextoSair(textoSair);

			setBotaoSair(new BotaoVirtual(margemDosBotoes,
				textoSair.getAlinhamentoDoPivo(),
				textoSair.getX(),
				textoSair.getY(),
				textoSair.getLargura(),
				textoSair.getAltura()));

			TextoEstatico textoControle = new TextoEstatico(alfabeto,
				gereTextoControle(),
				0.0f,
				AlinhamentoDoPivo.HORIZONTAL_CENTRO | AlinhamentoDoPivo.VERTICAL_CIMA,
				0.5f * larguraDaVista,
				alturaDaVista - (3.0f * tamanhoDoTexto));
			setTextoControle(textoControle);

			setBotaoControle(new BotaoVirtual(margemDosBotoes,
				textoControle.getAlinhamentoDoPivo(),
				textoControle.getX(),
				textoControle.getY(),
				textoControle.getLargura(),
				textoControle.getAltura()));

			listaDeElementosDeTela.adicioneAoInicio(fundo);
			listaDeElementosDeTela.adicioneAoInicio(textoPausado);
			listaDeElementosDeTela.adicioneAoInicio(textoSair);
			listaDeElementosDeTela.adicioneAoInicio(textoControle);

			// Não é possível mais pausar, pois o jogo não está mais valendo
			getTextoPausar().remova();
		}

		//------------------------------------------------------------------------------------------
		// Métodos acessores e modificadores
		//------------------------------------------------------------------------------------------

		private FadeEstatico getFundo() {
			return fundo;
		}

		private void setFundo(FadeEstatico fundo) {
			this.fundo = fundo;
		}

		private TextoEstatico getTextoPausado() {
			return textoPausado;
		}

		private void setTextoPausado(TextoEstatico textoPausado) {
			this.textoPausado = textoPausado;
		}

		private TextoEstatico getTextoSair() {
			return textoSair;
		}

		private void setTextoSair(TextoEstatico textoSair) {
			this.textoSair = textoSair;
		}

		private TextoEstatico getTextoControle() {
			return textoControle;
		}

		private void setTextoControle(TextoEstatico textoControle) {
			this.textoControle = textoControle;
		}

		private BotaoVirtual getBotaoSair() {
			return botaoSair;
		}

		private void setBotaoSair(BotaoVirtual botaoSair) {
			this.botaoSair = botaoSair;
		}

		private BotaoVirtual getBotaoControle() {
			return botaoControle;
		}

		private void setBotaoControle(BotaoVirtual botaoControle) {
			this.botaoControle = botaoControle;
		}

		//------------------------------------------------------------------------------------------
		// Métodos privados
		//------------------------------------------------------------------------------------------

		private void removaElementosDaTela() {
			// Remove todos os elementos da tela que eram necessários apenas para esse estado

			getFundo().removaEDestrua();
			getTextoPausado().removaEDestrua();
			getTextoSair().removaEDestrua();
			getTextoControle().removaEDestrua();
		}

		//------------------------------------------------------------------------------------------
		// Métodos públicos
		//------------------------------------------------------------------------------------------

		@Override
		public void perdeuInteratividadeComJogador() {
			// Nada a fazer durante a pausa
		}

		@Override
		public void botaoVoltarPressionado() {
			// Apenas retoma o jogo
			removaElementosDaTela();
			setProcessadorDeEstados(new ProcessadorDoJogo(false));
		}

		@Override
		public void posProcessamentoDoQuadro(float deltaSegundos) {
			// Verifica se o jogador tocou/clicou na tela para retomar ou encerrar o jogo, ou para
			// alterar o tipo de movimento
			Ponteiro ponteiro = Jogo.getJogo().getPonteiroPrincipal();

			if (ponteiro.isToqueRecemTerminado()) {
				// Se clicar/tocar botão controle, altera o tipo de movimento da nave, se
				// clicar/tocar no botão sair, sai o jogo, caso contrário, retoma o jogo
				if (getBotaoControle().contemPonteiro(ponteiro)) {
					Nave nave = getNave();
					nave.setControladaPorMovimento(!nave.isControladaPorMovimento());

					// Grava o tipo de controle para que da próxima vez o tipo de controle já
					// venha do jeito que o jogador configurou
					getPersistencia().setNaveControladaPorMovimento(nave.isControladaPorMovimento());

					getTextoControle().setTexto(gereTextoControle());
				} else if (getBotaoSair().contemPonteiro(ponteiro)) {
					// Confirma se o jogador deseja mesmo sair do jogo
					removaElementosDaTela();
					setProcessadorDeEstados(new ProcessadorDeConfirmacaoDeSaida());
				} else {
					// Apenas retoma o jogo
					removaElementosDaTela();
					setProcessadorDeEstados(new ProcessadorDoJogo(false));
				}
			}
		}

		@Override
		public void destrua() {
			// Vamos invalidar o objeto

			setFundo(null);
			setTextoPausado(null);
			setTextoSair(null);
			setTextoControle(null);
			setBotaoSair(null);
			setBotaoControle(null);
		}
	}

	private class ProcessadorDeConfirmacaoDeSaida implements ProcessadorDeEstados {
		//------------------------------------------------------------------------------------------
		// Campos privados
		//------------------------------------------------------------------------------------------

		private FadeEstatico fundo;
		private TextoEstatico textoConfirmacao, textoSim, textoNao;
		private BotaoVirtual botaoSim, botaoNao;

		//------------------------------------------------------------------------------------------
		// Construtores
		//------------------------------------------------------------------------------------------

		public ProcessadorDeConfirmacaoDeSaida() {
			// Para o processamento dos elementos
			ElementoDeTelaComPausa.setPausado(true);

			Jogo jogo = Jogo.getJogo();
			Tela tela = Tela.getTela();
			Alfabeto alfabeto = getAlfabeto();

			float margemDosBotoes = getMargemDosBotoes();
			float tamanhoDoTexto = getTamanhoDoTexto();
			float larguraDaVista = tela.getLarguraDaVista();
			float alturaDaVista = tela.getAlturaDaVista();

			// Configura o layout da tela de confirmação
			ListaDeElementosDeTela listaDeElementosDeTela = getListaDeElementosDeTela();

			// Cria o fundo translúcido que será utilizado como fundo da tela de confirmação
			FadeEstatico fundo = new FadeEstatico(getFolhaDeSprites(), OPACIDADE_DO_FUNDO);
			setFundo(fundo);

			// Cria os textos e botões que serão utilizados na tela de confirmação
			TextoEstatico textoConfirmacao = new TextoEstatico(alfabeto,
				jogo.texto(R.string.confirmacao),
				0.0f,
				AlinhamentoDoPivo.HORIZONTAL_CENTRO | AlinhamentoDoPivo.VERTICAL_CIMA,
				0.5f * larguraDaVista,
				(0.5f * alturaDaVista) - (2.0f * tamanhoDoTexto));
			setTextoConfirmacao(textoConfirmacao);

			TextoEstatico textoSim = new TextoEstatico(alfabeto,
				jogo.texto(R.string.sim),
				0.0f,
				AlinhamentoDoPivo.HORIZONTAL_CENTRO | AlinhamentoDoPivo.VERTICAL_CENTRO,
				0.25f * larguraDaVista,
				(0.5f * alturaDaVista) + (2.0f * tamanhoDoTexto));
			setTextoSim(textoSim);

			setBotaoSim(new BotaoVirtual(margemDosBotoes,
				textoSim.getAlinhamentoDoPivo(),
				textoSim.getX(),
				textoSim.getY(),
				textoSim.getLargura(),
				textoSim.getAltura()));

			TextoEstatico textoNao = new TextoEstatico(alfabeto,
				jogo.texto(R.string.nao),
				0.0f,
				AlinhamentoDoPivo.HORIZONTAL_CENTRO | AlinhamentoDoPivo.VERTICAL_CENTRO,
				0.75f * larguraDaVista,
				(0.5f * alturaDaVista) + (2.0f * tamanhoDoTexto));
			setTextoNao(textoNao);

			setBotaoNao(new BotaoVirtual(margemDosBotoes,
				textoNao.getAlinhamentoDoPivo(),
				textoNao.getX(),
				textoNao.getY(),
				textoNao.getLargura(),
				textoNao.getAltura()));

			listaDeElementosDeTela.adicioneAoInicio(fundo);
			listaDeElementosDeTela.adicioneAoInicio(textoConfirmacao);
			listaDeElementosDeTela.adicioneAoInicio(textoSim);
			listaDeElementosDeTela.adicioneAoInicio(textoNao);

			// Não é possível mais pausar, pois o jogo não está mais valendo
			getTextoPausar().remova();
		}

		//------------------------------------------------------------------------------------------
		// Métodos acessores e modificadores
		//------------------------------------------------------------------------------------------

		private FadeEstatico getFundo() {
			return fundo;
		}

		private void setFundo(FadeEstatico fundo) {
			this.fundo = fundo;
		}

		private TextoEstatico getTextoConfirmacao() {
			return textoConfirmacao;
		}

		private void setTextoConfirmacao(TextoEstatico textoConfirmacao) {
			this.textoConfirmacao = textoConfirmacao;
		}

		private TextoEstatico getTextoSim() {
			return textoSim;
		}

		private void setTextoSim(TextoEstatico textoSim) {
			this.textoSim = textoSim;
		}

		private TextoEstatico getTextoNao() {
			return textoNao;
		}

		private void setTextoNao(TextoEstatico textoNao) {
			this.textoNao = textoNao;
		}

		private BotaoVirtual getBotaoSim() {
			return botaoSim;
		}

		private void setBotaoSim(BotaoVirtual botaoSim) {
			this.botaoSim = botaoSim;
		}

		private BotaoVirtual getBotaoNao() {
			return botaoNao;
		}

		private void setBotaoNao(BotaoVirtual botaoNao) {
			this.botaoNao = botaoNao;
		}

		//------------------------------------------------------------------------------------------
		// Métodos privados
		//------------------------------------------------------------------------------------------

		private void removaElementosDaTela() {
			// Remove todos os elementos da tela que eram necessários apenas para esse estado

			getFundo().removaEDestrua();
			getTextoConfirmacao().removaEDestrua();
			getTextoSim().removaEDestrua();
			getTextoNao().removaEDestrua();
		}

		//------------------------------------------------------------------------------------------
		// Métodos públicos
		//------------------------------------------------------------------------------------------

		@Override
		public void perdeuInteratividadeComJogador() {
			// Nada a fazer durante a confirmação
		}

		@Override
		public void botaoVoltarPressionado() {
			// Apenas retoma o jogo
			removaElementosDaTela();
			setProcessadorDeEstados(new ProcessadorDoJogo(false));
		}

		@Override
		public void posProcessamentoDoQuadro(float deltaSegundos) {
			Ponteiro ponteiro = Jogo.getJogo().getPonteiroPrincipal();

			if (ponteiro.isToqueRecemTerminado()) {
				if (getBotaoSim().contemPonteiro(ponteiro)) {
					// Encerra o jogo
					setProcessadorDeEstados(new ProcessadorDeSaida(false));
				} else if (getBotaoNao().contemPonteiro(ponteiro)) {
					// Apenas retoma o jogo
					removaElementosDaTela();
					setProcessadorDeEstados(new ProcessadorDoJogo(false));
				}
			}
		}

		@Override
		public void destrua() {
			// Vamos invalidar o objeto

			setFundo(null);
			setTextoConfirmacao(null);
			setTextoSim(null);
			setTextoNao(null);
			setBotaoSim(null);
			setBotaoNao(null);
		}
	}

	private class ProcessadorDoGameOver implements ProcessadorDeEstados, TextoAnimado.Observador {
		//------------------------------------------------------------------------------------------
		// Campos privados
		//------------------------------------------------------------------------------------------

		private BotaoVirtual botaoTentarDeNovo, botaoSair;

		//------------------------------------------------------------------------------------------
		// Construtores
		//------------------------------------------------------------------------------------------

		public ProcessadorDoGameOver() {
			Jogo jogo = Jogo.getJogo();
			Tela tela = Tela.getTela();
			Alfabeto alfabeto = getAlfabeto();
			ListaDeElementosDeTela listaDeElementosDeTela = getListaDeElementosDeTela();

			float margemDosBotoes = getMargemDosBotoes();
			float larguraDaVista = tela.getLarguraDaVista();
			float alturaDaVista = tela.getAlturaDaVista();

			String texto;

			// Esse é o momento de atualizar o recorde
			int recorde = getPersistencia().getRecorde();
			int pontuacao = getPontuacao();
			if (pontuacao > recorde) {
				getPersistencia().setRecorde(pontuacao);
				// Final feliz :)
				texto = Jogo.getJogo().texto(R.string.parabens) + " " + pontuacao;
			} else {
				// Final triste :(
				texto = Jogo.getJogo().texto(R.string.game_over) + " " + recorde;
			}

			// Hora de atualizar o placar do Play Games, também
			ControleDoPlayGames.getControleDoPlayGames().atualizePlacar(pontuacao);

			Nave nave = getNave();

			// O texto do game over sairá da posição da nave, e irá até o centro da tela, crescendo
			TextoAnimado textoGameOver = new TextoAnimado(alfabeto,
				texto,
				0.0f,
				AlinhamentoDoPivo.HORIZONTAL_CENTRO | AlinhamentoDoPivo.VERTICAL_CENTRO,
				DURACAO_DO_GAME_OVER,
				InterpoladorDePontos.crieDesacelerado(
					new VetorFloat(nave.getX(), 0.5f * larguraDaVista),
					new VetorFloat(nave.getY(), nave.getY() - (2.0f * getTamanhoDoTexto()))
				));
			textoGameOver.setAlinhamentoHorizontalDasLinhas(AlinhamentoDoPivo.HORIZONTAL_CENTRO);
			textoGameOver.setInterpoladorDaEscalaX(Interpolador.crieAceleradoDesacelerado());
			textoGameOver.setInterpoladorDaEscalaY(Interpolador.crieAceleradoDesacelerado());
			textoGameOver.setObservador(this);

			// Cria os textos e os botões para tentar de novo ou sair de vez do jogo
			TextoAnimado textoTentarDeNovo = new TextoAnimado(alfabeto,
				jogo.texto(R.string.tentar_de_novo),
				0.0f,
				AlinhamentoDoPivo.HORIZONTAL_CENTRO | AlinhamentoDoPivo.VERTICAL_CENTRO,
				DURACAO_DO_GAME_OVER,
				InterpoladorDePontos.crieDesacelerado(
					new VetorFloat(nave.getX(), 0.25f * larguraDaVista),
					new VetorFloat(nave.getY(), 0.5f * alturaDaVista)
				));
			textoTentarDeNovo.setInterpoladorDaEscalaX(Interpolador.crieAceleradoDesacelerado());
			textoTentarDeNovo.setInterpoladorDaEscalaY(Interpolador.crieAceleradoDesacelerado());

			setBotaoTentarDeNovo(new BotaoVirtual(margemDosBotoes,
				textoTentarDeNovo.getAlinhamentoDoPivo(),
				0.25f * larguraDaVista,
				0.5f * alturaDaVista,
				textoTentarDeNovo.getLargura(),
				textoTentarDeNovo.getAltura()));

			TextoAnimado textoSair = new TextoAnimado(alfabeto,
				jogo.texto(R.string.sair),
				0.0f,
				AlinhamentoDoPivo.HORIZONTAL_CENTRO | AlinhamentoDoPivo.VERTICAL_CENTRO,
				DURACAO_DO_GAME_OVER,
				InterpoladorDePontos.crieDesacelerado(
					new VetorFloat(nave.getX(), 0.75f * larguraDaVista),
					new VetorFloat(nave.getY(), 0.5f * alturaDaVista)
				));
			textoSair.setInterpoladorDaEscalaX(Interpolador.crieAceleradoDesacelerado());
			textoSair.setInterpoladorDaEscalaY(Interpolador.crieAceleradoDesacelerado());

			setBotaoSair(new BotaoVirtual(margemDosBotoes,
				textoSair.getAlinhamentoDoPivo(),
				0.75f * larguraDaVista,
				0.5f * alturaDaVista,
				textoSair.getLargura(),
				textoSair.getAltura()));

			// Não deixa que o fade seja 100% opaco, para permitir que o jogador veja a horda de
			// inimigos se movendo ao fundo
			Fade fade = new Fade(getFolhaDeSprites(), DURACAO_DO_GAME_OVER, false);
			fade.setOpacidadeMaxima(OPACIDADE_DO_FUNDO);

			listaDeElementosDeTela.adicioneAoInicio(fade);
			listaDeElementosDeTela.adicioneAoInicio(textoGameOver);
			listaDeElementosDeTela.adicioneAoInicio(textoTentarDeNovo);
			listaDeElementosDeTela.adicioneAoInicio(textoSair);

			// Não é possível mais pausar, pois o jogo não está mais valendo
			getTextoPausar().remova();
		}

		//------------------------------------------------------------------------------------------
		// Métodos acessores e modificadores
		//------------------------------------------------------------------------------------------

		private BotaoVirtual getBotaoTentarDeNovo() {
			return botaoTentarDeNovo;
		}

		private void setBotaoTentarDeNovo(BotaoVirtual botaoTentarDeNovo) {
			this.botaoTentarDeNovo = botaoTentarDeNovo;
		}

		private BotaoVirtual getBotaoSair() {
			return botaoSair;
		}

		private void setBotaoSair(BotaoVirtual botaoSair) {
			this.botaoSair = botaoSair;
		}

		//------------------------------------------------------------------------------------------
		// Métodos públicos
		//------------------------------------------------------------------------------------------

		@Override
		public void animacaoDoTextoTerminada(TextoAnimado textoAnimado) {
			// De agora em diante o jogador poderá decidir se tenta de novo, ou se encerra o jogo
			setProcessadorDeEstados(new ProcessadorDoGameOverTerminado(getBotaoTentarDeNovo(), getBotaoSair()));
		}

		@Override
		public void perdeuInteratividadeComJogador() {
			// Nada a fazer durante o game over
		}

		@Override
		public void botaoVoltarPressionado() {
			// Apenas ignora o botão voltar durante o game over
		}

		@Override
		public void posProcessamentoDoQuadro(float deltaSegundos) {
			// Nada o que processar durante o game over, além da horda de inimigos

			// Move a horda de inimigos pela tela
			getHordaDeInimigos().processeUmQuadro(deltaSegundos);
		}

		@Override
		public void destrua() {
			// Vamos invalidar o objeto

			setBotaoTentarDeNovo(null);
			setBotaoSair(null);
		}
	}

	private class ProcessadorDoGameOverTerminado implements ProcessadorDeEstados {
		//------------------------------------------------------------------------------------------
		// Campos privados
		//------------------------------------------------------------------------------------------

		private BotaoVirtual botaoTentarDeNovo, botaoSair;

		//------------------------------------------------------------------------------------------
		// Construtores
		//------------------------------------------------------------------------------------------

		public ProcessadorDoGameOverTerminado(BotaoVirtual botaoTentarDeNovo, BotaoVirtual botaoSair) {
			setBotaoTentarDeNovo(botaoTentarDeNovo);
			setBotaoSair(botaoSair);
		}

		//------------------------------------------------------------------------------------------
		// Métodos acessores e modificadores
		//------------------------------------------------------------------------------------------

		private BotaoVirtual getBotaoTentarDeNovo() {
			return botaoTentarDeNovo;
		}

		private void setBotaoTentarDeNovo(BotaoVirtual botaoTentarDeNovo) {
			this.botaoTentarDeNovo = botaoTentarDeNovo;
		}

		private BotaoVirtual getBotaoSair() {
			return botaoSair;
		}

		private void setBotaoSair(BotaoVirtual botaoSair) {
			this.botaoSair = botaoSair;
		}

		//------------------------------------------------------------------------------------------
		// Métodos públicos
		//------------------------------------------------------------------------------------------

		@Override
		public void perdeuInteratividadeComJogador() {
			// Nada a fazer durante o game over
		}

		@Override
		public void botaoVoltarPressionado() {
			// Apenas ignora o botão voltar durante o game over
		}

		@Override
		public void posProcessamentoDoQuadro(float deltaSegundos) {
			// Move a horda de inimigos pela tela
			getHordaDeInimigos().processeUmQuadro(deltaSegundos);

			Ponteiro ponteiro = Jogo.getJogo().getPonteiroPrincipal();

			// Assim que o jogador clicar/tocar na tela, decidimos o que acontece
			if (Jogo.getJogo().getPonteiroPrincipal().isToqueRecemTerminado()) {
				if (getBotaoTentarDeNovo().contemPonteiro(ponteiro)) {
					// Reinicia o jogo, mas sem a explicação inicial
					setProcessadorDeEstados(new ProcessadorDeSaida(true));
				} else if (getBotaoSair().contemPonteiro(ponteiro)) {
					// Encerra jogo
					setProcessadorDeEstados(new ProcessadorDeSaida(false));
				}
			}
		}

		@Override
		public void destrua() {
			// Vamos invalidar o objeto

			setBotaoTentarDeNovo(null);
			setBotaoSair(null);
		}
	}

	private class ProcessadorDeSaida implements ProcessadorDeEstados, Fade.Observador {
		//------------------------------------------------------------------------------------------
		// Campos privados
		//------------------------------------------------------------------------------------------

		private final boolean reiniciando;

		//------------------------------------------------------------------------------------------
		// Construtores
		//------------------------------------------------------------------------------------------

		public ProcessadorDeSaida(boolean reiniciando) {
			// Pausa todos os elementos da tela e cria o fade que irá indicar visualmente a saída
			ElementoDeTelaComPausa.setPausado(true);

			this.reiniciando = reiniciando;

			Fade fade = new Fade(getFolhaDeSprites(), DURACAO_DO_FADE, false);
			fade.setObservador(this);
			getListaDeElementosDeTela().adicioneAoInicio(fade);
		}

		//------------------------------------------------------------------------------------------
		// Métodos acessores e modificadores
		//------------------------------------------------------------------------------------------

		private boolean isReiniciando() {
			return reiniciando;
		}

		//------------------------------------------------------------------------------------------
		// Métodos públicos
		//------------------------------------------------------------------------------------------

		@Override
		public void animacaoDoFadeTerminada(Fade fade) {
			// Quando a animação do fade de saída termina, ou voltamos para o cenário inicial, ou
			// voltamos para o início do cenário do jogo

			// Porém, além de mudar para o novo cenário, nós vamos enviar para aquele cenário alguns
			// de nossos recursos já criados, para acelerar o processo de mudança ;)

			// * Só não podemos esquecer de definir nossa folha de sprites para null, além de
			// remover o campo estelar da lista de elementos, para que esses recursos não sejam
			// destruídos

			Cenario cenario = (isReiniciando() ? new CenarioJogo(true) : new CenarioInicial());

			cenario.adicioneRecursoInicial("folhaDeSprites", getFolhaDeSprites());
			setFolhaDeSprites(null);

			CampoEstelar campoEstelar = getCampoEstelar();
			cenario.adicioneRecursoInicial("campoEstelar", campoEstelar);
			campoEstelar.remova();

			Jogo.getJogo().setCenarioSeguinte(cenario);
		}

		@Override
		public void perdeuInteratividadeComJogador() {
			// Nada a fazer durante a saída
		}

		@Override
		public void botaoVoltarPressionado() {
			// Apenas ignora o botão voltar durante a saída
		}

		@Override
		public void posProcessamentoDoQuadro(float deltaSegundos) {
			// Nada o que processar durante a saída
		}

		@Override
		public void destrua() {
			// Nada a fazer
		}
	}

	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private FolhaDeSprites folhaDeSprites;
	private CampoEstelar campoEstelar;
	private Nave nave;
	private HordaDeInimigos hordaDeInimigos;
	private Alfabeto alfabeto;
	private String stringPontos, stringVidas;
	private TextoEstatico textoPontuacao, textoVidas, textoPausar;
	private BotaoVirtual botaoPausar;
	private int pontuacao, hordasDestruidas;
	private float margemDosBotoes, tamanhoDoTexto, tempoTotalConquistaPacifista, tempoTotalConquistaInvencivel;
	private boolean conquistaPacifistaLiberada, conquistaInvencivelLiberada;
	private final boolean ignorandoExplicacao;
	private ProcessadorDeEstados processadorDeEstados;
	private Persistencia persistencia;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	public CenarioJogo(boolean ignorandoExplicacao) {
		this.ignorandoExplicacao = ignorandoExplicacao;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	private FolhaDeSprites getFolhaDeSprites() {
		return folhaDeSprites;
	}

	private void setFolhaDeSprites(FolhaDeSprites folhaDeSprites) {
		this.folhaDeSprites = folhaDeSprites;
	}

	private CampoEstelar getCampoEstelar() {
		return campoEstelar;
	}

	private void setCampoEstelar(CampoEstelar campoEstelar) {
		this.campoEstelar = campoEstelar;
	}

	private Nave getNave() {
		return nave;
	}

	private void setNave(Nave nave) {
		this.nave = nave;
	}

	private HordaDeInimigos getHordaDeInimigos() {
		return hordaDeInimigos;
	}

	private void setHordaDeInimigos(HordaDeInimigos hordaDeInimigos) {
		HordaDeInimigos hordaDeInimigosAntiga = getHordaDeInimigos();

		if (hordaDeInimigosAntiga != null) {
			hordaDeInimigosAntiga.destrua();
		}

		this.hordaDeInimigos = hordaDeInimigos;
	}

	private Alfabeto getAlfabeto() {
		return alfabeto;
	}

	private void setAlfabeto(Alfabeto alfabeto) {
		this.alfabeto = alfabeto;
	}

	private String getStringPontos() {
		return stringPontos;
	}

	private void setStringPontos(String stringPontos) {
		this.stringPontos = stringPontos;
	}

	private String getStringVidas() {
		return stringVidas;
	}

	private void setStringVidas(String stringVidas) {
		this.stringVidas = stringVidas;
	}

	private TextoEstatico getTextoPontuacao() {
		return textoPontuacao;
	}

	private void setTextoPontuacao(TextoEstatico textoPontuacao) {
		this.textoPontuacao = textoPontuacao;
	}

	private TextoEstatico getTextoVidas() {
		return textoVidas;
	}

	private void setTextoVidas(TextoEstatico textoVidas) {
		this.textoVidas = textoVidas;
	}

	private TextoEstatico getTextoPausar() {
		return textoPausar;
	}

	private void setTextoPausar(TextoEstatico textoPausar) {
		this.textoPausar = textoPausar;
	}

	private BotaoVirtual getBotaoPausar() {
		return botaoPausar;
	}

	private void setBotaoPausar(BotaoVirtual botaoPausar) {
		this.botaoPausar = botaoPausar;
	}

	private int getPontuacao() {
		return pontuacao;
	}

	private void setPontuacao(int pontuacao) {
		this.pontuacao = pontuacao;

		getTextoPontuacao().setTexto(getStringPontos() + pontuacao);
	}

	private int getHordasDestruidas() {
		return hordasDestruidas;
	}

	private void setHordasDestruidas(int hordasDestruidas) {
		this.hordasDestruidas = hordasDestruidas;
	}

	private float getMargemDosBotoes() {
		return margemDosBotoes;
	}

	private void setMargemDosBotoes(float margemDosBotoes) {
		this.margemDosBotoes = margemDosBotoes;
	}

	private float getTamanhoDoTexto() {
		return tamanhoDoTexto;
	}

	private void setTamanhoDoTexto(float tamanhoDoTexto) {
		this.tamanhoDoTexto = tamanhoDoTexto;
	}

	private float getTempoTotalConquistaPacifista() {
		return tempoTotalConquistaPacifista;
	}

	private void setTempoTotalConquistaPacifista(float tempoTotalConquistaPacifista) {
		this.tempoTotalConquistaPacifista = tempoTotalConquistaPacifista;
	}

	private float getTempoTotalConquistaInvencivel() {
		return tempoTotalConquistaInvencivel;
	}

	private void setTempoTotalConquistaInvencivel(float tempoTotalConquistaInvencivel) {
		this.tempoTotalConquistaInvencivel = tempoTotalConquistaInvencivel;
	}

	private boolean isConquistaPacifistaLiberada() {
		return conquistaPacifistaLiberada;
	}

	private void setConquistaPacifistaLiberada(boolean conquistaPacifistaLiberada) {
		this.conquistaPacifistaLiberada = conquistaPacifistaLiberada;
	}

	private boolean isConquistaInvencivelLiberada() {
		return conquistaInvencivelLiberada;
	}

	private void setConquistaInvencivelLiberada(boolean conquistaInvencivelLiberada) {
		this.conquistaInvencivelLiberada = conquistaInvencivelLiberada;
	}

	private boolean isIgnorandoExplicacao() {
		return ignorandoExplicacao;
	}

	private ProcessadorDeEstados getProcessadorDeEstados() {
		return processadorDeEstados;
	}

	private void setProcessadorDeEstados(ProcessadorDeEstados processadorDeEstados) {
		// Limpa o processador de estado anterior (caso ele exista)
		ProcessadorDeEstados processadorDeEstadosAntigo = getProcessadorDeEstados();
		if (processadorDeEstadosAntigo != null) {
			processadorDeEstadosAntigo.destrua();
		}

		this.processadorDeEstados = processadorDeEstados;
	}

	private Persistencia getPersistencia() {
		return persistencia;
	}

	private void setPersistencia(Persistencia persistencia) {
		this.persistencia = persistencia;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos privados e protegidos
	//----------------------------------------------------------------------------------------------

	private void crieNovaHordaDeInimigos() {
		// Quando uma horda de inimigos acaba, outra aparece logo em seguida, variando o estilo:
		// normal, aleatória, normal, aleatória, normal...

		HordaDeInimigos hordaDeInimigos = getHordaDeInimigos();
		boolean entradaAleatoria = (hordaDeInimigos != null && !hordaDeInimigos.isEntradaAleatoria());

		setHordaDeInimigos(new HordaDeInimigos(getNave(), this, entradaAleatoria));
	}

	private String gereTextoControle() {
		Jogo jogo = Jogo.getJogo();
		if (getNave().isControladaPorMovimento()) {
			return "  " + jogo.texto(R.string.controle_por_toque) + "\n> " + jogo.texto(R.string.controle_por_movimento);
		} else {
			return "> " + jogo.texto(R.string.controle_por_toque) + "\n  " + jogo.texto(R.string.controle_por_movimento);
		}
	}

	@Override
	public void inicieInternamente(ArmazenamentoDeRecursos armazenamentoDeRecursos) {
		// Configuração inicial do cenário

		// Vamos permitir que todos os elementos funcionem normalmente
		ElementoDeTelaComPausa.setPausado(false);

		Jogo jogo = Jogo.getJogo();
		Tela tela = Tela.getTela();

		// Um novo jogo, novas conquistas!
		ControleDoPlayGames.getControleDoPlayGames().reinicieConquistasDoJogoAtual();

		// Carrega as informações salvas do jogo
		Persistencia persistencia = new Persistencia();
		setPersistencia(persistencia);

		// Antes de criar nossa folha de sprites, verifica se o outro cenário já a enviou, caso
		// contrário, carrega a folha de sprites (usada para desenhar o campo estelar e o fade)
		FolhaDeSprites folhaDeSprites = (FolhaDeSprites)armazenamentoDeRecursos.obtenha("folhaDeSprites");
		if (folhaDeSprites == null) {
			folhaDeSprites = new FolhaDeSprites();
		}
		setFolhaDeSprites(folhaDeSprites);

		// Ajusta a vista da tela
		Vista.ajusteTela(folhaDeSprites);

		// Idem para o campo estelar
		CampoEstelar campoEstelar = (CampoEstelar)armazenamentoDeRecursos.obtenha("campoEstelar");
		if (campoEstelar == null) {
			campoEstelar = new CampoEstelar(folhaDeSprites);
		}
		setCampoEstelar(campoEstelar);

		// Vamos criar nossa nave!!!
		Nave nave = new Nave(folhaDeSprites, persistencia.isNaveControladaPorMovimento(), this);
		setNave(nave);

		float larguraDaVista = tela.getLarguraDaVista();

		float margemDosBotoes = folhaDeSprites.pixels(1.5f);
		float tamanhoDoTexto = folhaDeSprites.pixels(0.5f);

		setMargemDosBotoes(margemDosBotoes);
		setTamanhoDoTexto(tamanhoDoTexto);

		// Carrega todos os textos que serão utilizados (o + " " no final é porque o xml não
		// preserva espaços no início/fim)
		String explicacao = jogo.texto(R.string.explicacao);
		String explicacao_controle_por_toque = jogo.texto(R.string.explicacao_controle_por_toque);
		String explicacao_controle_por_movimento = jogo.texto(R.string.explicacao_controle_por_movimento);
		String explicacao_final = jogo.texto(R.string.explicacao_final);
		String pontos = jogo.texto(R.string.pontos) + " ";
		String vidas = jogo.texto(R.string.vidas) + " ";
		String pausar = jogo.texto(R.string.pausar);
		String pausado = jogo.texto(R.string.pausado);
		String tentar_de_novo = jogo.texto(R.string.tentar_de_novo);
		String sair = jogo.texto(R.string.sair);
		String confirmacao = jogo.texto(R.string.confirmacao);
		String sim = jogo.texto(R.string.sim);
		String nao = jogo.texto(R.string.nao);
		String parabens = jogo.texto(R.string.parabens);
		String game_over = jogo.texto(R.string.game_over);
		String controle_por_toque = jogo.texto(R.string.controle_por_toque);
		String controle_por_movimento = jogo.texto(R.string.controle_por_movimento);
		String numeros_e_simbolos = jogo.texto(R.string.numeros_e_simbolos);

		// Como esses dois textos são os mais utilizados, armazena para utilizar depois
		setStringPontos(pontos);
		setStringVidas(vidas);

		// Vamos criar o alfabeto que desenhará os textos da tela, somente com os caracteres que
		// serão necessários
		Alfabeto alfabeto = new Alfabeto(tela.getFonte8Bit(),
			tamanhoDoTexto,
			COR_DOS_TEXTOS,
			true,
			explicacao, explicacao_controle_por_toque, explicacao_controle_por_movimento, explicacao_final, pontos, vidas, pausar, pausado, tentar_de_novo, sair, confirmacao, sim, nao, parabens, game_over, controle_por_toque, controle_por_movimento, numeros_e_simbolos);
		setAlfabeto(alfabeto);

		// Cria todos os textos que aparecerão na tela, assim como os botões
		TextoEstatico textoPontuacao = new TextoEstatico(alfabeto,
			pontos + getPontuacao(),
			0.0f,
			AlinhamentoDoPivo.HORIZONTAL_ESQUERDO | AlinhamentoDoPivo.VERTICAL_CIMA,
			tamanhoDoTexto,
			tamanhoDoTexto);
		setTextoPontuacao(textoPontuacao);

		TextoEstatico textoVidas = new TextoEstatico(alfabeto,
			vidas + nave.getVidas(),
			0.0f,
			AlinhamentoDoPivo.HORIZONTAL_DIREITO | AlinhamentoDoPivo.VERTICAL_CIMA,
			larguraDaVista - tamanhoDoTexto,
			tamanhoDoTexto);
		setTextoVidas(textoVidas);

		TextoEstatico textoPausar = new TextoEstatico(alfabeto,
			pausar,
			0.0f,
			AlinhamentoDoPivo.HORIZONTAL_CENTRO | AlinhamentoDoPivo.VERTICAL_CIMA,
			0.5f * larguraDaVista,
			tamanhoDoTexto);
		setTextoPausar(textoPausar);

		setBotaoPausar(new BotaoVirtual(margemDosBotoes,
			textoPausar.getAlinhamentoDoPivo(),
			textoPausar.getX(),
			textoPausar.getY(),
			textoPausar.getLargura(),
			textoPausar.getAltura()));

		// Por fim, vamos adicionar os elementos à lista de elementos desse cenário, na ordem em que
		// desejamos que eles sejam desenhados (quem estiver no início será desenhado mais à frente)

		ListaDeElementosDeTela listaDeElementosDeTela = getListaDeElementosDeTela();

		listaDeElementosDeTela.adicioneAoInicio(campoEstelar);
		listaDeElementosDeTela.adicioneAoInicio(nave);
		listaDeElementosDeTela.adicioneAoInicio(textoPontuacao);
		listaDeElementosDeTela.adicioneAoInicio(textoVidas);

		// Configura o processador inicial do cenário
		setProcessadorDeEstados(new ProcessadorDeEntrada());
	}

	@Override
	protected void carregueInternamente() {
		// Nossos únicos recursos que precisam ser carregados, além dos elementos de tela, são a
		// folha de sprites e os alfabetos

		FolhaDeSprites folhaDeSprites = getFolhaDeSprites();
		folhaDeSprites.carregue();

		// Ajusta a vista da tela
		Vista.ajusteTela(folhaDeSprites);

		getAlfabeto().carregue();

		// Esse elemento pode ou não estar na lista de elementos de tela nesse momento, por isso
		// deve ser carregado manualmente
		getTextoPausar().carregue();

		// Queremos um fundo preto para nosso cenário
		Tela.getTela().corDoPreenchimento(0.0f, 0.0f, 0.0f);

		// Ao final, precisamos chamar o método carregueInternamente() da classe Cenário, para
		// permitir que ela carregue seus recursos (os elementos de tela)
		super.carregueInternamente();
	}

	@Override
	protected void libereInternamente() {
		// Vamos liberar toda a memória que não será mais utilizada

		libereComSeguranca(getFolhaDeSprites());
		libereComSeguranca(getAlfabeto());

		// Esse elemento pode ou não estar na lista de elementos de tela nesse momento, por isso
		// deve ser liberado manualmente
		libereComSeguranca(getTextoPausar());

		// Ao final, precisamos chamar o método libereInternamente() da classe Cenário, para
		// permitir que ela libere seus recursos (os elementos de tela)
		super.libereInternamente();
	}

	@Override
	protected void destruaInternamente() {
		// Vamos invalidar o objeto

		destruaComSeguranca(getFolhaDeSprites());
		setFolhaDeSprites(null);
		destruaComSeguranca(getAlfabeto());
		setAlfabeto(null);

		// Armazena o recorde e as preferências
		Persistencia persistencia = getPersistencia();
		if (persistencia != null) {
			persistencia.graveEmArquivo();
			persistencia.destrua();
			setPersistencia(null);
		}

		// Esse elemento pode ou não estar na lista de elementos de tela nesse momento, por isso
		// deve ser destruído manualmente
		destruaComSeguranca(getTextoPausar());
		setTextoPausar(null);

		setCampoEstelar(null);
		setNave(null);
		setHordaDeInimigos(null);
		setTextoPontuacao(null);
		setTextoVidas(null);
		setBotaoPausar(null);
		setProcessadorDeEstados(null);

		// Ao final, precisamos chamar o método destruaInternamente() da classe Cenário, para
		// permitir que ela destrua seus recursos (os elementos de tela)
		super.destruaInternamente();
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	@Override
	public void vidaDaNaveAlterada(Nave nave) {
		int vidas = nave.getVidas();

		getTextoVidas().setTexto(getStringVidas() + vidas);

		// Reinicia o tempo total da conquista invencível
		setTempoTotalConquistaInvencivel(0.0f);

		if (vidas == 0) {
			// Pois é.... acabou o jogo!
			setProcessadorDeEstados(new ProcessadorDoGameOver());
		}
	}

	@Override
	public void inimigoExplodiu(Inimigo inimigo) {
		// Só vamos contar um ponto caso a nave ainda esteja viva (às vezes a nave explode, mas seus
		// tiros ainda continuam na tela, e podem acertar um inimigo, o que não deve contar ponto)
		if (getNave().getVidas() > 0) {
			setPontuacao(getPontuacao() + 1);

			// Reinicia o tempo total da conquista pacifista
			setTempoTotalConquistaPacifista(0.0f);

			// Quando uma horda de inimigos acaba, outra aparece logo em seguida
			if (getHordaDeInimigos().getInimigosRestantes() == 0) {

				// Vamos verificar e liberar as conquistas pela quantidade de hordas destruídas
				int hordasDestruidas = getHordasDestruidas() + 1;
				setHordasDestruidas(hordasDestruidas);

				if (hordasDestruidas == 1) {
					ControleDoPlayGames.getControleDoPlayGames().libereConquista(ControleDoPlayGames.CONQUISTA_PRIMEIRA_VEZ);
				} else if (hordasDestruidas == 3) {
					ControleDoPlayGames.getControleDoPlayGames().libereConquista(ControleDoPlayGames.CONQUISTA_DESTRUIDOR);
				} else if (hordasDestruidas == 5) {
					ControleDoPlayGames.getControleDoPlayGames().libereConquista(ControleDoPlayGames.CONQUISTA_ANIQUILADOR);
				}

				crieNovaHordaDeInimigos();
			}
		}
	}

	@Override
	public void interatividadeComJogadorPerdida() {
		getProcessadorDeEstados().perdeuInteratividadeComJogador();
	}

	@Override
	public void botaoVoltarPressionado() {
		getProcessadorDeEstados().botaoVoltarPressionado();
	}

	@Override
	public void processeEDesenheUmQuadro(float deltaSegundos) {
		// Deixa a classe Cenario fazer o desenho básico da tela
		super.processeEDesenheUmQuadro(deltaSegundos);

		// Agora vamos fazer o pós-processamento do cenário
		getProcessadorDeEstados().posProcessamentoDoQuadro(deltaSegundos);
	}
}
