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
package br.com.carlosrafaelgn.navinha.jogo.elementos;

import br.com.carlosrafaelgn.navinha.jogo.desenho.FolhaDeSprites;
import br.com.carlosrafaelgn.navinha.modelo.desenho.CoordenadasDeModelo;
import br.com.carlosrafaelgn.navinha.modelo.desenho.CoordenadasDeTextura;
import br.com.carlosrafaelgn.navinha.modelo.desenho.Tela;

public final class Tiro extends ElementoDeTelaComPausa {
	//----------------------------------------------------------------------------------------------
	// Constantes
	//----------------------------------------------------------------------------------------------

	private static final float VELOCIDADE_EM_UNIDADES_POR_SEGUNDO = 8.0f;

	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private FolhaDeSprites folhaDeSprites;
	private float x, y, velocidade;
	private boolean tiroDaNave;
	private Class<? extends AlvoDeTiro> classeDoAlvo;
	private CoordenadasDeModelo coordenadasDeModelo;
	private CoordenadasDeModelo coordenadasDeModeloDosLimites;
	private CoordenadasDeTextura coordenadasDeTextura;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	public Tiro(FolhaDeSprites folhaDeSprites, float x, float y, boolean tiroDaNave) {
		// Não é necessário adicionar o tiro ao sistema de colisões, porque nenhum outro elemento
		// fará um teste de colisões para detectar se está colidindo com um tiro (apenas o tiro
		// testa as colisões)

		setFolhaDeSprites(folhaDeSprites);
		setTiroDaNave(tiroDaNave);

		// Os tiros da nave sobem, enquanto que os dos inimigos descem
		if (tiroDaNave) {
			setVelocidade(-folhaDeSprites.pixels(VELOCIDADE_EM_UNIDADES_POR_SEGUNDO));
			// Os tiros da nave só acertam inimigos
			setClasseDoAlvo(Inimigo.class);
		} else {
			setVelocidade(folhaDeSprites.pixels(VELOCIDADE_EM_UNIDADES_POR_SEGUNDO));
			// Os tiros dos inimigos só acertam a nave
			setClasseDoAlvo(Nave.class);
		}

		carregueInternamente();

		// Define a posição inicial apenas depois de carregado
		setXY(x, y);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	public FolhaDeSprites getFolhaDeSprites() {
		return folhaDeSprites;
	}

	private void setFolhaDeSprites(FolhaDeSprites folhaDeSprites) {
		this.folhaDeSprites = folhaDeSprites;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	private void setXY(float x, float y) {
		this.x = x;
		this.y = y;

		atualizeAreaLimite();
	}

	public float getVelocidade() {
		return velocidade;
	}

	private void setVelocidade(float velocidade) {
		this.velocidade = velocidade;
	}

	public boolean isTiroDaNave() {
		return tiroDaNave;
	}

	private void setTiroDaNave(boolean tiroDaNave) {
		this.tiroDaNave = tiroDaNave;
	}

	private Class<? extends AlvoDeTiro> getClasseDoAlvo() {
		return classeDoAlvo;
	}

	private void setClasseDoAlvo(Class<? extends AlvoDeTiro> classeDoAlvo) {
		this.classeDoAlvo = classeDoAlvo;
	}

	private CoordenadasDeModelo getCoordenadasDeModelo() {
		return coordenadasDeModelo;
	}

	private void setCoordenadasDeModelo(CoordenadasDeModelo coordenadasDeModelo) {
		this.coordenadasDeModelo = coordenadasDeModelo;
	}

	private CoordenadasDeModelo getCoordenadasDeModeloDosLimites() {
		return coordenadasDeModeloDosLimites;
	}

	private void setCoordenadasDeModeloDosLimites(CoordenadasDeModelo coordenadasDeModeloDosLimites) {
		this.coordenadasDeModeloDosLimites = coordenadasDeModeloDosLimites;
	}

	private CoordenadasDeTextura getCoordenadasDeTextura() {
		return coordenadasDeTextura;
	}

	private void setCoordenadasDeTextura(CoordenadasDeTextura coordenadasDeTextura) {
		this.coordenadasDeTextura = coordenadasDeTextura;
	}

	@Override
	public boolean isCarregado() {
		// Nossa forma de detectar se o recurso foi carregado ou não (cada recurso faz isso de
		// jeitos diferentes)
		return (getCoordenadasDeModelo() != null);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos privados e protegidos
	//----------------------------------------------------------------------------------------------

	private void atualizeAreaLimite() {
		altereAreaLimite(getCoordenadasDeModeloDosLimites(), getX(), getY());
	}

	@Override
	protected void carregueInternamente() {
		FolhaDeSprites folhaDeSprites = getFolhaDeSprites();

		if (isTiroDaNave()) {
			setCoordenadasDeModelo(folhaDeSprites.getCoordenadasDeModeloDoTiroDaNave());
			setCoordenadasDeModeloDosLimites(folhaDeSprites.getCoordenadasDeModeloDosLimitesDoTiroDaNave());
			setCoordenadasDeTextura(folhaDeSprites.getCoordenadasDeTexturaDoTiroDaNave());
		} else {
			setCoordenadasDeModelo(folhaDeSprites.getCoordenadasDeModeloDoTiroDoInimigo());
			setCoordenadasDeModeloDosLimites(folhaDeSprites.getCoordenadasDeModeloDosLimitesDoTiroDoInimigo());
			setCoordenadasDeTextura(folhaDeSprites.getCoordenadasDeTexturaDoTiroDoInimigo());
		}

		atualizeAreaLimite();
	}

	@Override
	protected void libereInternamente() {
		// Vamos liberar toda a memória que não será mais utilizada

		setCoordenadasDeModelo(null);
		setCoordenadasDeModeloDosLimites(null);
		setCoordenadasDeTextura(null);
	}

	@Override
	protected void destruaInternamente() {
		// Vamos invalidar o objeto

		setFolhaDeSprites(null);
		setClasseDoAlvo(null);

		// Ao final, precisamos chamar o método destruaInternamente() da classe ElementoDeTela, para
		// permitir que ela destrua seus recursos
		super.destruaInternamente();
	}

	@Override
	protected void processeUmQuadroSemPausa(float deltaSegundos) {
		// s = s0 + v.t :)
		float y = getY() + (getVelocidade() * deltaSegundos);
		setXY(getX(), y);

		float alturaLimite = getFolhaDeSprites().pixels(1.0f);

		if (y <= -alturaLimite || y >= (Tela.getTela().getAlturaDaVista() + alturaLimite)) {
			// Se o tiro saiu da área visível da vista, ele pode ser removido
			removaEDestrua();
		} else {
			// Caso contrário, vamos testar se o tiro colidiu com um alvo, e em caso afirmativo,
			// avisa o alvo
			AlvoDeTiro alvoDeTiro = getLista().primeiroElementoQueColide(this, getClasseDoAlvo());
			if (alvoDeTiro != null) {
				alvoDeTiro.acertadoPorUmTiro(this);
				removaEDestrua();
			}
		}
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	@Override
	public void desenheUmQuadro() {
		Tela.getTela().desenhe(getFolhaDeSprites().getImagem(), getCoordenadasDeModelo(), 1.0f, getCoordenadasDeTextura(), getX(), getY());
	}
}
