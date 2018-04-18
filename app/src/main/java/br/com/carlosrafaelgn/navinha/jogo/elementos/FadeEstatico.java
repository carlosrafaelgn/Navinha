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
import br.com.carlosrafaelgn.navinha.modelo.elemento.ElementoDeTela;

public class FadeEstatico extends ElementoDeTela {
	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private FolhaDeSprites folhaDeSprites;
	private float opacidade;
	private CoordenadasDeModelo coordenadasDeModelo;
	private CoordenadasDeTextura coordenadasDeTextura;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	public FadeEstatico(FolhaDeSprites folhaDeSprites, float opacidade) {
		setFolhaDeSprites(folhaDeSprites);
		setOpacidade(opacidade);

		carregueInternamente();
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	public final FolhaDeSprites getFolhaDeSprites() {
		return folhaDeSprites;
	}

	private void setFolhaDeSprites(FolhaDeSprites folhaDeSprites) {
		this.folhaDeSprites = folhaDeSprites;
	}

	public final float getOpacidade() {
		return opacidade;
	}

	public final void setOpacidade(float opacidade) {
		this.opacidade = opacidade;
	}

	private CoordenadasDeModelo getCoordenadasDeModelo() {
		return coordenadasDeModelo;
	}

	private void setCoordenadasDeModelo(CoordenadasDeModelo coordenadasDeModelo) {
		this.coordenadasDeModelo = coordenadasDeModelo;
	}

	private CoordenadasDeTextura getCoordenadasDeTextura() {
		return coordenadasDeTextura;
	}

	private void setCoordenadasDeTextura(CoordenadasDeTextura coordenadasDeTextura) {
		this.coordenadasDeTextura = coordenadasDeTextura;
	}

	@Override
	public final boolean isCarregado() {
		// Nossa forma de detectar se o recurso foi carregado ou não (cada recurso faz isso de
		// jeitos diferentes)
		return (getCoordenadasDeModelo() != null);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos privados e protegidos
	//----------------------------------------------------------------------------------------------

	@Override
	protected void carregueInternamente() {
		Tela tela = Tela.getTela();

		setCoordenadasDeModelo(new CoordenadasDeModelo(0.0f, 0.0f, tela.getLarguraDaVista(), tela.getAlturaDaVista()));
		setCoordenadasDeTextura(getFolhaDeSprites().getCoordenadasDeTexturaDoFade());
	}

	@Override
	protected void libereInternamente() {
		// Vamos liberar toda a memória que não será mais utilizada

		setCoordenadasDeModelo(null);
		setCoordenadasDeTextura(null);
	}

	@Override
	protected void destruaInternamente() {
		// Vamos invalidar o objeto

		setFolhaDeSprites(null);

		// Ao final, precisamos chamar o método destruaInternamente() da classe ElementoDeTela, para
		// permitir que ela destrua seus recursos
		super.destruaInternamente();
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	@Override
	public void processeUmQuadro(float deltaSegundos) {
	}

	@Override
	public void desenheUmQuadro() {
		Tela.getTela().desenhe(getFolhaDeSprites().getImagem(), getCoordenadasDeModelo(), getOpacidade(), getCoordenadasDeTextura());
	}
}
