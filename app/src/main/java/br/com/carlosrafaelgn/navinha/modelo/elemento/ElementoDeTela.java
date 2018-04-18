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
package br.com.carlosrafaelgn.navinha.modelo.elemento;

import br.com.carlosrafaelgn.navinha.modelo.desenho.CoordenadasDeModelo;
import br.com.carlosrafaelgn.navinha.modelo.recurso.Recurso;

public abstract class ElementoDeTela extends Recurso {
	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private ElementoDeTela elementoAnterior, elementoSeguinte;
	private ListaDeElementosDeTela lista;
	// Não vamos utilizar um objeto retângulo aqui, pois utilizaremos os valores soltos
	private float areaLimiteEsquerda, areaLimiteCima, areaLimiteDireita, areaLimiteBaixo;
	private boolean marcadoParaRemocao, parteDoSistemaDeColisoes;

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	final ElementoDeTela getElementoAnterior() {
		return elementoAnterior;
	}

	final void setElementoAnterior(ElementoDeTela elementoAnterior) {
		this.elementoAnterior = elementoAnterior;
	}

	final ElementoDeTela getElementoSeguinte() {
		return elementoSeguinte;
	}

	final void setElementoSeguinte(ElementoDeTela elementoSeguinte) {
		this.elementoSeguinte = elementoSeguinte;
	}

	public final ListaDeElementosDeTela getLista() {
		return lista;
	}

	final void setLista(ListaDeElementosDeTela lista) {
		this.lista = lista;
	}

	public final float getAreaLimiteEsquerda() {
		return areaLimiteEsquerda;
	}

	private void setAreaLimiteEsquerda(float areaLimiteEsquerda) {
		this.areaLimiteEsquerda = areaLimiteEsquerda;
	}

	public final float getAreaLimiteCima() {
		return areaLimiteCima;
	}

	private void setAreaLimiteCima(float areaLimiteCima) {
		this.areaLimiteCima = areaLimiteCima;
	}

	public final float getAreaLimiteDireita() {
		return areaLimiteDireita;
	}

	private void setAreaLimiteDireita(float areaLimiteDireita) {
		this.areaLimiteDireita = areaLimiteDireita;
	}

	public final float getAreaLimiteBaixo() {
		return areaLimiteBaixo;
	}

	private void setAreaLimiteBaixo(float areaLimiteBaixo) {
		this.areaLimiteBaixo = areaLimiteBaixo;
	}

	final boolean isMarcadoParaRemocao() {
		return marcadoParaRemocao;
	}

	final void setMarcadoParaRemocao(boolean marcadoParaRemocao) {
		this.marcadoParaRemocao = marcadoParaRemocao;
	}

	public final boolean isParteDoSistemaDeColisoes() {
		return parteDoSistemaDeColisoes;
	}

	public final void setParteDoSistemaDeColisoes(boolean parteDoSistemaDeColisoes) {
		if (this.parteDoSistemaDeColisoes == parteDoSistemaDeColisoes) {
			return;
		}

		this.parteDoSistemaDeColisoes = parteDoSistemaDeColisoes;

		// Atualiza o estado desse elemento perante à lista
		ListaDeElementosDeTela lista = getLista();
		if (lista != null) {
			if (parteDoSistemaDeColisoes) {
				lista.adicioneAoSistemaDeColisoes(this);
			} else {
				lista.removaDoSistemaDeColisoes(this);
			}
		}
	}

	//----------------------------------------------------------------------------------------------
	// Métodos privados e protegidos
	//----------------------------------------------------------------------------------------------

	@Override
	protected void destruaInternamente() {
		// Vamos invalidar o objeto

		setElementoAnterior(null);
		setElementoSeguinte(null);
		setLista(null);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	public final void remova() {
		ListaDeElementosDeTela lista = getLista();
		if (lista != null) {
			lista.remova(this);
		}
	}

	public final void removaEDestrua() {
		ListaDeElementosDeTela lista = getLista();
		if (lista != null) {
			lista.removaEDestrua(this);
		} else {
			destrua();
		}
	}

	public final boolean limitesColidem(ElementoDeTela elementoDeTela) {
		// Apenas determina se os retângulos limítrofes dos dois elementos fazem alguma intersecção
		return ((getAreaLimiteEsquerda() < elementoDeTela.getAreaLimiteDireita()) &&
			(elementoDeTela.getAreaLimiteEsquerda() < getAreaLimiteDireita()) &&
			(getAreaLimiteCima() < elementoDeTela.getAreaLimiteBaixo()) &&
			(elementoDeTela.getAreaLimiteCima() < getAreaLimiteBaixo()));
	}

	public final boolean limitesContemPonto(float x, float y) {
		// Apenas determina se o ponto dado está dentro do retângulo limítrofe
		return ((getAreaLimiteEsquerda() <= x) &&
			(x < getAreaLimiteDireita()) &&
			(getAreaLimiteCima() <= y) &&
			(y < getAreaLimiteBaixo()));
	}

	public boolean colideCom(ElementoDeTela elementoDeTela) {
		// A implementação padrão para detecção de colisões se baseia apenas em uma única
		// intersecção de retângulos, o que oferece resultados aceitáveis com um tempo de
		// processamento rápido, considerando que o formato dos elementos realmente seja próximo ao
		// de um retângulo
		// Outras implementações podem utilizar regras de colisões mais complexas, como intersecção
		// de um ou mais polígonos, círculos etc...
		return ((getAreaLimiteEsquerda() < elementoDeTela.getAreaLimiteDireita()) &&
			(elementoDeTela.getAreaLimiteEsquerda() < getAreaLimiteDireita()) &&
			(getAreaLimiteCima() < elementoDeTela.getAreaLimiteBaixo()) &&
			(elementoDeTela.getAreaLimiteCima() < getAreaLimiteBaixo()));
	}

	public boolean contemPonto(float x, float y) {
		// A implementação padrão para o teste de pertinência de ponto se baseia apenas em um único
		// teste de pertinência de ponto em retângulo, o que oferece resultados aceitáveis com um
		// tempo de processamento rápido, considerando que o formato dos elementos realmente seja
		// próximo ao de um retângulo
		// Outras implementações podem utilizar testes de pertinência mais complexos, como
		// pertinência de ponto em círculo, elipse ou até mesmo em um ou mais polígonos
		return ((getAreaLimiteEsquerda() <= x) &&
			(x < getAreaLimiteDireita()) &&
			(getAreaLimiteCima() <= y) &&
			(y < getAreaLimiteBaixo()));
	}

	public final void altereAreaLimite(CoordenadasDeModelo coordenadasDeModelo, float x, float y) {
		setAreaLimiteEsquerda(coordenadasDeModelo.getEsquerda() + x);
		setAreaLimiteCima(coordenadasDeModelo.getCima() + y);
		setAreaLimiteDireita(coordenadasDeModelo.getDireita() + x);
		setAreaLimiteBaixo(coordenadasDeModelo.getBaixo() + y);

		ListaDeElementosDeTela lista = getLista();
		if (isParteDoSistemaDeColisoes() && lista != null) {
			// Pedimos à lista de elementos à qual esse elemento pertence, para que ela faça as
			// alterações necessárias internamente
			lista.areaLimiteAlterada(this);
		}
	}

	public final void altereAreaLimite(float esquerda, float cima, float direita, float baixo) {
		setAreaLimiteEsquerda(esquerda);
		setAreaLimiteCima(cima);
		setAreaLimiteDireita(direita);
		setAreaLimiteBaixo(baixo);

		ListaDeElementosDeTela lista = getLista();
		if (isParteDoSistemaDeColisoes() && lista != null) {
			// Pedimos à lista de elementos à qual esse elemento pertence, para que ela faça as
			// alterações necessárias internamente
			lista.areaLimiteAlterada(this);
		}
	}

	public abstract void processeUmQuadro(float deltaSegundos);

	public abstract void desenheUmQuadro();
}
