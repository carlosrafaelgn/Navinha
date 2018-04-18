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
package br.com.carlosrafaelgn.navinha.modelo.dados;

import java.util.ArrayList;
import java.util.Arrays;

import br.com.carlosrafaelgn.navinha.modelo.jogo.Jogo;

public final class ListaEmbaralhavel<E> extends ArrayList<E> {
	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private E[] armazenamentoTemporario;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	public ListaEmbaralhavel() {
	}

	public ListaEmbaralhavel(int capacidade) {
		super(capacidade);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos privados e protegidos
	//----------------------------------------------------------------------------------------------

	private E[] getArmazenamentoTemporario() {
		return armazenamentoTemporario;
	}

	private void setArmazenamentoTemporario(E[] armazenamentoTemporario) {
		this.armazenamentoTemporario = armazenamentoTemporario;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	public void embaralhe() {
		E[] armazenamentoTemporario = getArmazenamentoTemporario();
		int tamanho = size();

		if (tamanho < 2) {
			// Nada para embaralhar
			return;
		}

		if (armazenamentoTemporario == null || armazenamentoTemporario.length < tamanho) {
			armazenamentoTemporario = (E[])new Object[tamanho];
			setArmazenamentoTemporario(armazenamentoTemporario);
		}

		toArray(armazenamentoTemporario);

		Jogo jogo = Jogo.getJogo();

		// Remove um objeto de uma posição aleatória do armazenamento temporário e o coloca de volta
		// na lista correta
		int destino = 0;
		while (tamanho > 0) {
			int indice = jogo.numeroAleatorio(tamanho);

			E objeto = armazenamentoTemporario[indice];

			// Remove o objeto na posição indice
			System.arraycopy(armazenamentoTemporario, indice + 1, armazenamentoTemporario, indice, tamanho - indice - 1);

			// Armazena o objeto na posição correta
			set(destino, objeto);
			destino++;

			tamanho--;
		}

		// Ajuda o garbage collector :)
		Arrays.fill(armazenamentoTemporario, 0, size(), null);
	}
}
