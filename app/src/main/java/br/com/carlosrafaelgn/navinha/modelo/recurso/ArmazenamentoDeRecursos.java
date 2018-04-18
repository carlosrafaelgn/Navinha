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
package br.com.carlosrafaelgn.navinha.modelo.recurso;

import java.util.HashMap;

public final class ArmazenamentoDeRecursos {
	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private HashMap<String, Recurso> recursos;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	public ArmazenamentoDeRecursos() {
		setRecursos(new HashMap<String, Recurso>());
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	private HashMap<String, Recurso> getRecursos() {
		return recursos;
	}

	private void setRecursos(HashMap<String, Recurso> recursos) {
		this.recursos = recursos;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	public void adicione(String nome, Recurso recurso) {
		// Vamos apenas adicionar o recurso dado, a não ser que já exista um recurso com esse nome

		if (recurso == null) {
			return;
		}

		if (nome == null) {
			throw new IllegalArgumentException("O nome do recurso é nulo");
		}

		HashMap<String, Recurso> recursos = getRecursos();

		if (recursos.containsKey(nome)) {
			throw new IllegalArgumentException("Já existe um recurso com esse nome");
		}

		recursos.put(nome, recurso);
	}

	public Recurso obtenha(String nome) {
		// Utilizamos remove em vez de get para que, uma vez obtido, o recurso saia do armazenamento
		return getRecursos().remove(nome);
	}

	public void destrua() {
		// Destrói todos os recursos que sobraram dentro do armazenamento

		HashMap<String, Recurso> recursos = getRecursos();
		if (recursos != null) {
			for (Recurso recurso : recursos.values()) {
				recurso.destrua();
			}
			recursos.clear();
			setRecursos(null);
		}
	}
}
