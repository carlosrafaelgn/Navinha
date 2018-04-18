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
import br.com.carlosrafaelgn.navinha.modelo.animacao.contadores.Contador;
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

public class CenarioInicial extends Cenario implements ControleDoPlayGames.Observador {
	//----------------------------------------------------------------------------------------------
	// Constantes
	//----------------------------------------------------------------------------------------------

	private static final int COR_DO_TITULO = 0xffffffff;
	private static final int COR_DO_SUBTITULO = 0xff4499ff;

	private static final float DURACAO_DA_ANIMACAO_DOS_TEXTOS = 2.0f;
	private static final float DURACAO_DO_FADE = 1.0f;
	private static final float DURACAO_DO_FADE_DO_ALERTA = 0.5f;
	private static final float DURACAO_DO_PISCA_PISCA = 1.0f;

	private static final int ESTADO_ENTRANDO = 0;
	private static final int ESTADO_ALERTA = 1;
	private static final int ESTADO_FADE_DO_ALERTA = 2;
	private static final int ESTADO_TITULO = 3;
	private static final int ESTADO_PARADO = 4;
	private static final int ESTADO_SAINDO = 5;

	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private int estado;
	private FolhaDeSprites folhaDeSprites;
	private CampoEstelar campoEstelar;
	private Alfabeto alfabetoTitulo, alfabetoSubtitulo;
	private TextoAnimado textoAlerta, textoTitulo, textoSubtitulo, textoPlayGames, textoConquistas, textoPlacar, textoSair;
	private BotaoVirtual botaoPlayGames, botaoConquistas, botaoPlacar, botaoSair;
	private Fade fade;

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	private int getEstado() {
		return estado;
	}

	private void setEstado(int estado) {
		this.estado = estado;
	}

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

	private Alfabeto getAlfabetoTitulo() {
		return alfabetoTitulo;
	}

	private void setAlfabetoTitulo(Alfabeto alfabetoTitulo) {
		this.alfabetoTitulo = alfabetoTitulo;
	}

	private Alfabeto getAlfabetoSubtitulo() {
		return alfabetoSubtitulo;
	}

	private void setAlfabetoSubtitulo(Alfabeto alfabetoSubtitulo) {
		this.alfabetoSubtitulo = alfabetoSubtitulo;
	}

	private TextoAnimado getTextoAlerta() {
		return textoAlerta;
	}

	private void setTextoAlerta(TextoAnimado textoAlerta) {
		this.textoAlerta = textoAlerta;
	}

	private TextoAnimado getTextoTitulo() {
		return textoTitulo;
	}

	private void setTextoTitulo(TextoAnimado textoTitulo) {
		this.textoTitulo = textoTitulo;
	}

	private TextoAnimado getTextoSubtitulo() {
		return textoSubtitulo;
	}

	private void setTextoSubtitulo(TextoAnimado textoSubtitulo) {
		this.textoSubtitulo = textoSubtitulo;
	}

	private TextoAnimado getTextoPlayGames() {
		return textoPlayGames;
	}

	private void setTextoPlayGames(TextoAnimado textoPlayGames) {
		this.textoPlayGames = textoPlayGames;
	}

	private TextoAnimado getTextoConquistas() {
		return textoConquistas;
	}

	private void setTextoConquistas(TextoAnimado textoConquistas) {
		this.textoConquistas = textoConquistas;
	}

	private TextoAnimado getTextoPlacar() {
		return textoPlacar;
	}

	private void setTextoPlacar(TextoAnimado textoPlacar) {
		this.textoPlacar = textoPlacar;
	}

	private TextoAnimado getTextoSair() {
		return textoSair;
	}

	private void setTextoSair(TextoAnimado textoSair) {
		this.textoSair = textoSair;
	}

	private BotaoVirtual getBotaoPlayGames() {
		return botaoPlayGames;
	}

	private void setBotaoPlayGames(BotaoVirtual botaoPlayGames) {
		this.botaoPlayGames = botaoPlayGames;
	}

	private BotaoVirtual getBotaoConquistas() {
		return botaoConquistas;
	}

	private void setBotaoConquistas(BotaoVirtual botaoConquistas) {
		this.botaoConquistas = botaoConquistas;
	}

	private BotaoVirtual getBotaoPlacar() {
		return botaoPlacar;
	}

	private void setBotaoPlacar(BotaoVirtual botaoPlacar) {
		this.botaoPlacar = botaoPlacar;
	}

	private BotaoVirtual getBotaoSair() {
		return botaoSair;
	}

	private void setBotaoSair(BotaoVirtual botaoSair) {
		this.botaoSair = botaoSair;
	}

	private Fade getFade() {
		return fade;
	}

	private void setFade(Fade fade) {
		this.fade = fade;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos privados e protegidos
	//----------------------------------------------------------------------------------------------

	@Override
	protected void inicieInternamente(ArmazenamentoDeRecursos armazenamentoDeRecursos) {
		// Configuração inicial do cenário

		// Vamos permitir que todos os elementos funcionem normalmente (no caso do cenário inicial,
		// é apenas o campo estelar)
		ElementoDeTelaComPausa.setPausado(false);

		setEstado(ESTADO_ENTRANDO);

		// Estamos interessados no estado da conexão do Play Games (para alterar o texto na tela)
		ControleDoPlayGames.getControleDoPlayGames().setObservador(this);

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

		Jogo jogo = Jogo.getJogo();
		Tela tela = Tela.getTela();

		// Carrega todos os textos que serão utilizados
		String alerta = jogo.texto(R.string.alerta);
		String titulo = jogo.texto(R.string.app_name);
		String subtitulo = jogo.texto(R.string.subtitulo);
		String entrar_play_games = jogo.texto(R.string.entrar_play_games);
		String sair_play_games = jogo.texto(R.string.sair_play_games);
		String conquistas = jogo.texto(R.string.conquistas);
		String placar = jogo.texto(R.string.placar);
		String sair = jogo.texto(R.string.sair);

		float larguraDaVista = tela.getLarguraDaVista();
		float alturaDaVista = tela.getAlturaDaVista();

		// Cria o título, fazendo com que ele passe por alguns pontos na tela
		float tamanhoDoTextoDoTitulo = folhaDeSprites.pixels(2.0f);

		Alfabeto alfabetoTitulo = new Alfabeto(tela.getFonte8Bit(),
			tamanhoDoTextoDoTitulo,
			COR_DO_TITULO,
			true,
			titulo);
		setAlfabetoTitulo(alfabetoTitulo);

		TextoAnimado textoTitulo = new TextoAnimado(alfabetoTitulo,
			titulo,
			0.0f,
			AlinhamentoDoPivo.HORIZONTAL_CENTRO | AlinhamentoDoPivo.VERTICAL_CENTRO,
			DURACAO_DA_ANIMACAO_DOS_TEXTOS,
			InterpoladorDePontos.crieSpline(
				new VetorFloat(0.0f                   , 0.25f * larguraDaVista, 0.5f * larguraDaVista),
				new VetorFloat(-tamanhoDoTextoDoTitulo, 0.4f * alturaDaVista  , 0.5f * alturaDaVista)
			));
		textoTitulo.setInterpoladorDoAngulo(Interpolador.crieValores(Interpolador.crieDesacelerado(), new VetorFloat(-3.141592653f, 0.0f)));
		textoTitulo.setInterpoladorDaEscalaX(Interpolador.crieAcelerado());
		textoTitulo.setInterpoladorDaEscalaY(Interpolador.crieAcelerado());
		setTextoTitulo(textoTitulo);

		float tamanhoDoTextoDoSubtitulo = folhaDeSprites.pixels(0.5f);

		Alfabeto alfabetoSubtitulo = new Alfabeto(tela.getFonte8Bit(),
			tamanhoDoTextoDoSubtitulo,
			COR_DO_SUBTITULO,
			true,
			alerta, subtitulo, entrar_play_games, sair_play_games, conquistas, placar, sair);
		setAlfabetoSubtitulo(alfabetoSubtitulo);

		// Cria o alerta, que ficará estático no centro da tela
		TextoAnimado textoAlerta = new TextoAnimado(alfabetoSubtitulo,
			alerta,
			0.5f * tamanhoDoTextoDoSubtitulo,
			AlinhamentoDoPivo.HORIZONTAL_CENTRO | AlinhamentoDoPivo.VERTICAL_CENTRO,
			DURACAO_DO_FADE_DO_ALERTA,
			InterpoladorDePontos.crieConstante(
				0.5f * larguraDaVista,
				0.5f * alturaDaVista
			));
		textoAlerta.setAlinhamentoHorizontalDasLinhas(AlinhamentoDoPivo.HORIZONTAL_CENTRO);
		setTextoAlerta(textoAlerta);

		// Cria o subtítulo, fazendo com que ele apareça pela parte inferior da tela
		setTextoSubtitulo(new TextoAnimado(alfabetoSubtitulo,
			subtitulo,
			0.0f,
			AlinhamentoDoPivo.HORIZONTAL_CENTRO | AlinhamentoDoPivo.VERTICAL_CIMA,
			DURACAO_DA_ANIMACAO_DOS_TEXTOS,
			InterpoladorDePontos.crieDesacelerado(
				new VetorFloat(0.5f * larguraDaVista, 0.5f * larguraDaVista),
				new VetorFloat(alturaDaVista        , (0.5f * alturaDaVista) + (4.0f * tamanhoDoTextoDoSubtitulo))
			)));

		// Define a margem que será utilizada em todos os botões
		float margemDosBotoes = tamanhoDoTextoDoSubtitulo;

		// Cria o texto do Play Games, fazendo com que ele entre na tela pelo canto superior
		// esquerdo, em um movimento diagonal
		TextoAnimado textoPlayGames = new TextoAnimado(alfabetoSubtitulo,
			entrar_play_games,
			0.0f,
			AlinhamentoDoPivo.HORIZONTAL_ESQUERDO | AlinhamentoDoPivo.VERTICAL_CIMA,
			DURACAO_DA_ANIMACAO_DOS_TEXTOS,
			InterpoladorDePontos.crieDesacelerado(
				new VetorFloat(-(5.0f * tamanhoDoTextoDoSubtitulo), tamanhoDoTextoDoSubtitulo),
				new VetorFloat(-tamanhoDoTextoDoSubtitulo, tamanhoDoTextoDoSubtitulo)
			));
		setTextoPlayGames(textoPlayGames);

		// Cria o botão do Play Games, na posição final do texto Play Games
		setBotaoPlayGames(new BotaoVirtual(margemDosBotoes,
			textoPlayGames.getAlinhamentoDoPivo(),
			tamanhoDoTextoDoSubtitulo,
			tamanhoDoTextoDoSubtitulo,
			textoPlayGames.getLargura(),
			textoPlayGames.getAltura()));

		// Depois de criar o botão, testa o estado do Play Games, para alterar o texto, conforme o
		// necessário
		ControleDoPlayGames controleDoPlayGames = ControleDoPlayGames.getControleDoPlayGames();
		if (controleDoPlayGames != null && controleDoPlayGames.isConectado()) {
			textoPlayGames.setTexto(sair_play_games);
		}

		// Cria o texto das conquistas, fazendo com que ele entre na tela pelo canto superior
		// esquerdo, em um movimento diagonal, posicionando-o abaixo do botão Play Games
		TextoAnimado textoConquistas = new TextoAnimado(alfabetoSubtitulo,
			conquistas,
			0.0f,
			AlinhamentoDoPivo.HORIZONTAL_ESQUERDO | AlinhamentoDoPivo.VERTICAL_CIMA,
			DURACAO_DA_ANIMACAO_DOS_TEXTOS,
			InterpoladorDePontos.crieDesacelerado(
				new VetorFloat(-(5.0f * tamanhoDoTextoDoSubtitulo), tamanhoDoTextoDoSubtitulo),
				new VetorFloat(-tamanhoDoTextoDoSubtitulo, getBotaoPlayGames().getBaixo() + margemDosBotoes)
			));
		setTextoConquistas(textoConquistas);

		// Cria o botão das conquistas, na posição final do texto conquistas
		setBotaoConquistas(new BotaoVirtual(margemDosBotoes,
			textoConquistas.getAlinhamentoDoPivo(),
			tamanhoDoTextoDoSubtitulo,
			getBotaoPlayGames().getBaixo() + margemDosBotoes,
			textoConquistas.getLargura(),
			textoConquistas.getAltura()));

		// Cria o texto do placar, fazendo com que ele entre na tela pelo canto superior esquerdo,
		// em um movimento diagonal, posicionando-o abaixo do botão conquistas
		TextoAnimado textoPlacar = new TextoAnimado(alfabetoSubtitulo,
			placar,
			0.0f,
			AlinhamentoDoPivo.HORIZONTAL_ESQUERDO | AlinhamentoDoPivo.VERTICAL_CIMA,
			DURACAO_DA_ANIMACAO_DOS_TEXTOS,
			InterpoladorDePontos.crieDesacelerado(
				new VetorFloat(-(5.0f * tamanhoDoTextoDoSubtitulo), tamanhoDoTextoDoSubtitulo),
				new VetorFloat(-tamanhoDoTextoDoSubtitulo, getBotaoConquistas().getBaixo() + margemDosBotoes)
			));
		setTextoPlacar(textoPlacar);

		// Cria o botão do placar, na posição final do texto placar
		setBotaoPlacar(new BotaoVirtual(margemDosBotoes,
			textoPlacar.getAlinhamentoDoPivo(),
			tamanhoDoTextoDoSubtitulo,
			getBotaoConquistas().getBaixo() + tamanhoDoTextoDoSubtitulo,
			textoPlacar.getLargura(),
			textoPlacar.getAltura()));

		// Cria o texto sair, fazendo com que ele entre na tela pelo canto superior direito, em um
		// movimento diagonal
		TextoAnimado textoSair = new TextoAnimado(alfabetoSubtitulo,
			sair,
			0.0f,
			AlinhamentoDoPivo.HORIZONTAL_DIREITO | AlinhamentoDoPivo.VERTICAL_CIMA,
			DURACAO_DA_ANIMACAO_DOS_TEXTOS,
			InterpoladorDePontos.crieDesacelerado(
				new VetorFloat(larguraDaVista + (5.0f * tamanhoDoTextoDoSubtitulo), larguraDaVista - tamanhoDoTextoDoSubtitulo),
				new VetorFloat(-tamanhoDoTextoDoSubtitulo, tamanhoDoTextoDoSubtitulo)
			));
		setTextoSair(textoSair);

		// A margem do botão sair é maior
		margemDosBotoes = folhaDeSprites.pixels(1.5f);

		// Cria o botão sair, na posição final do texto sair
		setBotaoSair(new BotaoVirtual(margemDosBotoes,
			textoSair.getAlinhamentoDoPivo(),
			larguraDaVista - tamanhoDoTextoDoSubtitulo,
			tamanhoDoTextoDoSubtitulo,
			textoSair.getLargura(),
			textoSair.getAltura()));

		// Cria o fade
		Fade fade = new Fade(folhaDeSprites, DURACAO_DO_FADE, true);
		setFade(fade);

		// Por fim, vamos adicionar os elementos à lista de elementos desse cenário, na ordem em que
		// desejamos que eles sejam desenhados (quem estiver no início será desenhado mais à frente)
		ListaDeElementosDeTela listaDeElementosDeTela = getListaDeElementosDeTela();

		listaDeElementosDeTela.adicioneAoInicio(campoEstelar);
		listaDeElementosDeTela.adicioneAoInicio(textoAlerta);
		listaDeElementosDeTela.adicioneAoInicio(fade);
	}

	@Override
	protected void carregueInternamente() {
		// Nossos únicos recursos que precisam ser carregados, além dos elementos de tela, são a
		// folha de sprites e os alfabetos

		FolhaDeSprites folhaDeSprites = getFolhaDeSprites();
		folhaDeSprites.carregue();

		// Ajusta a vista da tela
		Vista.ajusteTela(folhaDeSprites);

		getAlfabetoTitulo().carregue();
		getAlfabetoSubtitulo().carregue();

		// Esses elementos podem ou não estar na lista de elementos de tela nesse momento, por isso
		// devem ser carregados manualmente
		getTextoTitulo().carregue();
		getTextoSubtitulo().carregue();
		getTextoPlayGames().carregue();
		getTextoConquistas().carregue();
		getTextoPlacar().carregue();
		getTextoSair().carregue();

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

		libereComSeguranca(getAlfabetoTitulo());
		libereComSeguranca(getAlfabetoSubtitulo());

		// Esses elementos podem ou não estar na lista de elementos de tela nesse momento, por isso
		// devem ser liberados manualmente
		libereComSeguranca(getTextoTitulo());
		libereComSeguranca(getTextoSubtitulo());
		libereComSeguranca(getTextoPlayGames());
		libereComSeguranca(getTextoConquistas());
		libereComSeguranca(getTextoPlacar());
		libereComSeguranca(getTextoSair());

		// Ao final, precisamos chamar o método libereInternamente() da classe Cenário, para
		// permitir que ela libere seus recursos (os elementos de tela)
		super.libereInternamente();
	}

	@Override
	protected void destruaInternamente() {
		// Vamos invalidar o objeto

		// Não estamos mais interessados nas alterações do estado da conexão do Play Games
		ControleDoPlayGames.getControleDoPlayGames().setObservador(null);

		destruaComSeguranca(getFolhaDeSprites());
		setFolhaDeSprites(null);
		destruaComSeguranca(getAlfabetoTitulo());
		setAlfabetoTitulo(null);
		destruaComSeguranca(getAlfabetoSubtitulo());
		setAlfabetoSubtitulo(null);

		// Esses elementos podem ou não estar na lista de elementos de tela nesse momento, por isso
		// devem ser destruídos manualmente
		destruaComSeguranca(getTextoTitulo());
		setTextoTitulo(null);
		destruaComSeguranca(getTextoSubtitulo());
		setTextoSubtitulo(null);
		destruaComSeguranca(getTextoPlayGames());
		setTextoPlayGames(null);
		destruaComSeguranca(getTextoConquistas());
		setTextoConquistas(null);
		destruaComSeguranca(getTextoPlacar());
		setTextoPlacar(null);
		destruaComSeguranca(getTextoSair());
		setTextoSair(null);

		setCampoEstelar(null);
		setTextoAlerta(null);
		setBotaoPlayGames(null);
		setBotaoConquistas(null);
		setBotaoPlacar(null);
		setBotaoSair(null);
		setFade(null);

		// Ao final, precisamos chamar o método destruaInternamente() da classe Cenário, para
		// permitir que ela destrua seus recursos (os elementos de tela)
		super.destruaInternamente();
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	@Override
	public void processeEDesenheUmQuadro(float deltaSegundos) {
		// Deixa a classe Cenario fazer o desenho básico da tela
		super.processeEDesenheUmQuadro(deltaSegundos);

		// Agora vamos fazer o pós-processamento do cenário inicial, que utiliza uma máquina de
		// estados simples, controlada pela animações dos elementos e pelos cliques/toques na tela

		Fade fade;
		TextoAnimado textoTitulo;

		switch (getEstado()) {
		case ESTADO_ENTRANDO:
			// Os estados ESTADO_ENTRANDO e ESTADO_SAINDO são controlados pelo estado da animação do
			// fade

			fade = getFade();

			if (fade.isAnimacaoTerminada()) {
				// Quando o fade tiver acabado basta removê-lo da lista e ir para o próximo estado

				setEstado(ESTADO_ALERTA);

				fade.removaEDestrua();
				setFade(null);
			}
			break;

		case ESTADO_ALERTA:
			// Para sair do estado ESTADO_ALERTA, e ir para o próximo estado, o jogador deve
			// clicar/tocar a tela

			if (Jogo.getJogo().getPonteiroPrincipal().isToqueRecemTerminado()) {
				setEstado(ESTADO_FADE_DO_ALERTA);

				// Faz o alerta sumir devagar
				TextoAnimado alerta = getTextoAlerta();
				alerta.setAnimacaoInvertida(true);
				alerta.setInterpoladorDoAlpha(Interpolador.crieAceleradoDesacelerado());
				alerta.reinicieAnimacao(DURACAO_DO_FADE_DO_ALERTA);
			}
			break;

		case ESTADO_FADE_DO_ALERTA:
			// Para sair do estado ESTADO_FADE_DO_ALERTA, e ir para o próximo estado, a animação do
			// alerta deve acabar

			TextoAnimado alerta = getTextoAlerta();
			if (alerta.isAnimacaoTerminada()) {
				setEstado(ESTADO_TITULO);

				// Remove o alerta da tela
				alerta.removaEDestrua();
				setTextoAlerta(null);

				// Adiciona os novos elementos
				ListaDeElementosDeTela listaDeElementosDeTela = getListaDeElementosDeTela();
				listaDeElementosDeTela.adicioneAoInicio(getTextoTitulo());
				listaDeElementosDeTela.adicioneAoInicio(getTextoSubtitulo());
				listaDeElementosDeTela.adicioneAoInicio(getTextoPlayGames());
				listaDeElementosDeTela.adicioneAoInicio(getTextoConquistas());
				listaDeElementosDeTela.adicioneAoInicio(getTextoPlacar());
				listaDeElementosDeTela.adicioneAoInicio(getTextoSair());
			}
			break;

		case ESTADO_TITULO:
			// O estado ESTADO_TITULO é controlado pelo estado da animação do título (cuja duração é
			// a mesma duração das outras animações na tela)

			textoTitulo = getTextoTitulo();
			if (textoTitulo.isAnimacaoTerminada()) {
				setEstado(ESTADO_PARADO);

				// De agora em diante o título ficará parado na tela, enquanto que o subtítulo
				// ficará "piscando" na tela, também parado no mesmo lugar (os outros elementos
				// também ficarão parados na tela, visto que seus contadores são do tipo UmaVez, só
				// estamos alterando o título aqui, para ir preparando para o que virá depois)
				textoTitulo.setInterpoladorDePontos(InterpoladorDePontos.crieConstante(
					textoTitulo.getX(),
					textoTitulo.getY()
				));
				textoTitulo.setInterpoladorDoAngulo(null);
				textoTitulo.setInterpoladorDaEscalaX(null);
				textoTitulo.setInterpoladorDaEscalaY(null);

				TextoAnimado textoSubtitulo = getTextoSubtitulo();
				textoSubtitulo.setInterpoladorDePontos(InterpoladorDePontos.crieConstante(
					textoSubtitulo.getX(),
					textoSubtitulo.getY()
				));
				textoSubtitulo.setInterpoladorDoAlpha(Interpolador.crieDegrau());
				textoSubtitulo.reinicieAnimacao(DURACAO_DO_PISCA_PISCA, Contador.LOOPING);
			}
			break;

		case ESTADO_PARADO:
			// Para sair do estado ESTADO_PARADO, e ir para o próximo estado, o jogador deve
			// clicar/tocar a tela (a não ser que o clique/toque ocorra no botão sair, quando na
			// verdade, encerramos o jogo)

			Jogo jogo = Jogo.getJogo();

			Ponteiro ponteiro = jogo.getPonteiroPrincipal();

			if (ponteiro.isToqueRecemTerminado()) {
				if (getBotaoSair().contemPonteiro(ponteiro)) {
					// Pois é... vamos ficando por aqui
					jogo.encerre();
					return;
				} else if (getBotaoPlayGames().contemPonteiro(ponteiro)) {
					// Conecta ou desconecta do Play Games, dependendo do estado atual
					ControleDoPlayGames controleDoPlayGames = ControleDoPlayGames.getControleDoPlayGames();
					if (controleDoPlayGames != null) {
						if (controleDoPlayGames.isConectado()) {
							controleDoPlayGames.desconecte();
							getTextoPlayGames().setTexto(jogo.texto(R.string.entrar_play_games));
						} else {
							controleDoPlayGames.conecte();
							// Não define o texto ainda, pois pode ser que a conexão ainda não tenha
							// sido concluída
						}
					}
					return;
				} else if (getBotaoConquistas().contemPonteiro(ponteiro)) {
					// Se estiver conectado, exibe as conquistas, caso contrário, conecta-se ao
					// Play Games
					ControleDoPlayGames controleDoPlayGames = ControleDoPlayGames.getControleDoPlayGames();
					if (controleDoPlayGames != null) {
						if (controleDoPlayGames.isConectado()) {
							controleDoPlayGames.exibaConquistas();
						} else {
							controleDoPlayGames.conecte();
							// Não define o texto ainda, pois pode ser que a conexão ainda não tenha
							// sido concluída
						}
					}
					return;
				} else if (getBotaoPlacar().contemPonteiro(ponteiro)) {
					// Se estiver conectado, exibe o placar, caso contrário, conecta-se ao Play
					// Games
					ControleDoPlayGames controleDoPlayGames = ControleDoPlayGames.getControleDoPlayGames();
					if (controleDoPlayGames != null) {
						if (controleDoPlayGames.isConectado()) {
							controleDoPlayGames.exibaPlacar();
						} else {
							controleDoPlayGames.conecte();
							// Não define o texto ainda, pois pode ser que a conexão ainda não tenha
							// sido concluída
						}
					}
					return;
				}

				setEstado(ESTADO_SAINDO);

				// Faz com que o título passe a piscar, assim como o subtítulo
				textoTitulo = getTextoTitulo();
				textoTitulo.setInterpoladorDoAlpha(Interpolador.crieDegrau());
				textoTitulo.reinicieAnimacao(0.2f * DURACAO_DO_PISCA_PISCA, Contador.LOOPING);

				// Reinicia a animação do subtítulo, fazendo com que ela seja mais rápida
				getTextoSubtitulo().reinicieAnimacao(0.2f * DURACAO_DO_PISCA_PISCA, Contador.LOOPING);

				// Reinicia a animação dos textos Play Games e sair, fazendo com que elas sejam
				// executadas de trás para frente
				TextoAnimado textoPlayGames = getTextoPlayGames();
				textoPlayGames.reinicieAnimacao(0.5f * DURACAO_DA_ANIMACAO_DOS_TEXTOS);
				textoPlayGames.setAnimacaoInvertida(true);

				TextoAnimado textoConquistas = getTextoConquistas();
				textoConquistas.reinicieAnimacao(0.5f * DURACAO_DA_ANIMACAO_DOS_TEXTOS);
				textoConquistas.setAnimacaoInvertida(true);

				TextoAnimado textoPlacar = getTextoPlacar();
				textoPlacar.reinicieAnimacao(0.5f * DURACAO_DA_ANIMACAO_DOS_TEXTOS);
				textoPlacar.setAnimacaoInvertida(true);

				TextoAnimado textoSair = getTextoSair();
				textoSair.reinicieAnimacao(0.5f * DURACAO_DA_ANIMACAO_DOS_TEXTOS);
				textoSair.setAnimacaoInvertida(true);

				// Recria o fade, e adiciona ao final da lista de elementos
				fade = new Fade(getFolhaDeSprites(), DURACAO_DO_FADE, false);
				getListaDeElementosDeTela().adicioneAoInicio(fade);
				setFade(fade);
			}
			break;

		case ESTADO_SAINDO:
			fade = getFade();

			if (fade.isAnimacaoTerminada()) {
				// Além de mudar para o cenário do jogo, nós vamos enviar para aquele cenário alguns
				// de nossos recursos já criados, para acelerar o processo de mudança ;)

				// * Só não podemos esquecer de definir nossa folha de sprites para null, além de
				// remover o campo estelar da lista de elementos, para que esses recursos não sejam
				// destruídos

				CenarioJogo cenarioJogo = new CenarioJogo(false);

				cenarioJogo.adicioneRecursoInicial("folhaDeSprites", getFolhaDeSprites());
				setFolhaDeSprites(null);

				CampoEstelar campoEstelar = getCampoEstelar();
				cenarioJogo.adicioneRecursoInicial("campoEstelar", campoEstelar);
				campoEstelar.remova();

				Jogo.getJogo().setCenarioSeguinte(cenarioJogo);
			}
			break;
		}
	}

	@Override
	public void conexaoAlterada() {
		TextoAnimado textoPlayGames = getTextoPlayGames();
		if (textoPlayGames != null) {
			textoPlayGames.setTexto(Jogo.getJogo().texto(ControleDoPlayGames.getControleDoPlayGames().isConectado() ?
				R.string.sair_play_games :
				R.string.entrar_play_games
			));
		}
	}
}
