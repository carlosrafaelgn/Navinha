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
package br.com.carlosrafaelgn.navinha.modelo.dados.imutavel;

public final class Vetor<E> {
	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private final E[] itens;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	public Vetor(E item0) {
		this.itens = (E[])new Object[] { item0 };
	}

	@SuppressWarnings("unchecked")
	public Vetor(E item0, E item1) {
		this.itens = (E[])new Object[] { item0, item1 };
	}

	@SuppressWarnings("unchecked")
	public Vetor(E item0, E item1, E item2) {
		this.itens = (E[])new Object[] { item0, item1, item2 };
	}

	@SuppressWarnings("unchecked")
	public Vetor(E item0, E item1, E item2, E item3) {
		this.itens = (E[])new Object[] { item0, item1, item2, item3 };
	}

	@SuppressWarnings("unchecked")
	public Vetor(E item0, E item1, E item2, E item3, E item4) {
		this.itens = (E[])new Object[] { item0, item1, item2, item3, item4 };
	}

	@SuppressWarnings("unchecked")
	public Vetor(E item0, E item1, E item2, E item3, E item4, E item5) {
		this.itens = (E[])new Object[] { item0, item1, item2, item3, item4, item5 };
	}

	@SafeVarargs
	public Vetor(E... itens) {
		// Proteção contra alterações externas
		this.itens = itens.clone();
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	private E[] getItens() {
		return itens;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	public int comprimento() {
		return getItens().length;
	}

	public E primeiro() {
		return getItens()[0];
	}

	public E ultimo() {
		return getItens()[getItens().length - 1];
	}

	public E item(int indice) {
		return getItens()[indice];
	}
}
