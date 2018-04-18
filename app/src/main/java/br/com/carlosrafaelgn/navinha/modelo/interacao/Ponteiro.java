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
package br.com.carlosrafaelgn.navinha.modelo.interacao;

import br.com.carlosrafaelgn.navinha.modelo.sincronizacao.MutexSimples;

public final class Ponteiro {
	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private final int id;
	private final MutexSimples controleDeAtualizacao;
	private float x, y;
	private boolean pressionado, pressionamentoAlteradoDesdeUltimoQuadro;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	public Ponteiro(int id) {
		this.id = id;
		this.controleDeAtualizacao = new MutexSimples();
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	public int getId() {
		return id;
	}

	private MutexSimples getControleDeAtualizacao() {
		return controleDeAtualizacao;
	}

	public float getX() {
		return x;
	}

	private void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	private void setY(float y) {
		this.y = y;
	}

	public boolean isPressionado() {
		return pressionado;
	}

	private void setPressionado(boolean pressionado) {
		setPressionamentoAlteradoDesdeUltimoQuadro(this.isPressionado() != pressionado);
		this.pressionado = pressionado;
	}

	public boolean isPressionamentoAlteradoDesdeUltimoQuadro() {
		return pressionamentoAlteradoDesdeUltimoQuadro;
	}

	private void setPressionamentoAlteradoDesdeUltimoQuadro(boolean pressionamentoAlteradoDesdeUltimoQuadro) {
		this.pressionamentoAlteradoDesdeUltimoQuadro = pressionamentoAlteradoDesdeUltimoQuadro;
	}

	public boolean isToqueRecemTerminado() {
		// Indica se o jogador acabou de soltar, depois de ter clicado/tocado a tela
		return (isPressionamentoAlteradoDesdeUltimoQuadro() && !isPressionado());
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	public void copie(Ponteiro ponteiro) {
		// Chamar ponteiro.getControleDeAtualizacao() fere os princípios de OO, mas nesse caso não
		// há outra forma... É necessário garantir atomicidade na atualização dos dados do ponteiro!
		try {
			ponteiro.getControleDeAtualizacao().entre0();

			setX(ponteiro.getX());
			setY(ponteiro.getY());
			setPressionado(ponteiro.isPressionado());
		} finally {
			ponteiro.getControleDeAtualizacao().saia0();
		}
	}

	public void atualize(float x, float y, boolean pressionado) {
		try {
			getControleDeAtualizacao().entre1();

			setX(x);
			setY(y);
			setPressionado(pressionado);
		} finally {
			getControleDeAtualizacao().saia1();
		}
	}
}
