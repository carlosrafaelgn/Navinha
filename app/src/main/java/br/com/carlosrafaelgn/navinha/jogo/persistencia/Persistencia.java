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
package br.com.carlosrafaelgn.navinha.jogo.persistencia;

import br.com.carlosrafaelgn.navinha.modelo.dados.persistencia.ArmazenamentoPersistente;

public final class Persistencia {
	//----------------------------------------------------------------------------------------------
	// Constantes
	//----------------------------------------------------------------------------------------------

	private static final int CHAVE_RECORDE = 1;

	private static final int BIT_NAVE_CONTROLADA_POR_MOVIMENTO = 0;

	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private ArmazenamentoPersistente armazenamentoPersistente;
	private boolean modificado;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	public Persistencia() {
		// Tenta abrir o arquivo de configurações, e se não for possível, cria um armazenamento em
		// branco
		ArmazenamentoPersistente armazenamentoPersistente = ArmazenamentoPersistente.carregueDoArquivo("configuracoes");
		if (armazenamentoPersistente == null) {
			armazenamentoPersistente = new ArmazenamentoPersistente();
		}
		setArmazenamentoPersistente(armazenamentoPersistente);
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	private ArmazenamentoPersistente getArmazenamentoPersistente() {
		return armazenamentoPersistente;
	}

	private void setArmazenamentoPersistente(ArmazenamentoPersistente armazenamentoPersistente) {
		this.armazenamentoPersistente = armazenamentoPersistente;
	}

	private boolean isModificado() {
		return modificado;
	}

	private void setModificado(boolean modificado) {
		this.modificado = modificado;
	}

	// Repare nos exemplos de encapsulamento abaixo (sequer existem na classe Persistencia para
	// armazenar essas duas propriedades)
	public int getRecorde() {
		return getArmazenamentoPersistente().getInt(CHAVE_RECORDE);
	}

	public void setRecorde(int recorde) {
		if (getRecorde() != recorde) {
			setModificado(true);
			getArmazenamentoPersistente().put(CHAVE_RECORDE, recorde);
		}
	}

	public boolean isNaveControladaPorMovimento() {
		return getArmazenamentoPersistente().getBit(BIT_NAVE_CONTROLADA_POR_MOVIMENTO);
	}

	public void setNaveControladaPorMovimento(boolean naveControladaPorMovimento) {
		if (isNaveControladaPorMovimento() != naveControladaPorMovimento) {
			setModificado(true);
			getArmazenamentoPersistente().putBit(BIT_NAVE_CONTROLADA_POR_MOVIMENTO, naveControladaPorMovimento);
		}
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	public boolean graveEmArquivo() {
		// Grava apenas se algo foi modificado
		if (isModificado()) {
			if (!getArmazenamentoPersistente().graveEmArquivo("configuracoes")) {
				return false;
			}
			setModificado(false);
		}
		return true;
	}

	public void destrua() {
		// Vamos liberar toda a memória que não será mais utilizada e invalidar o objeto

		ArmazenamentoPersistente armazenamentoPersistente = getArmazenamentoPersistente();
		if (armazenamentoPersistente != null) {
			armazenamentoPersistente.destrua();
			setArmazenamentoPersistente(null);
		}
	}
}
