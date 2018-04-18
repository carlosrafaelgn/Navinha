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
package br.com.carlosrafaelgn.navinha.modelo.sincronizacao;

// Uma implementação simples do algoritmo de Peterson
public final class MutexSimples {
	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private volatile boolean entrada0Desejada, entrada1Desejada;
	private volatile int vez;

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	private boolean isEntrada0Desejada() {
		return entrada0Desejada;
	}

	private void setEntrada0Desejada(boolean entrada0Desejada) {
		this.entrada0Desejada = entrada0Desejada;
	}

	private boolean isEntrada1Desejada() {
		return entrada1Desejada;
	}

	private void setEntrada1Desejada(boolean entrada1Desejada) {
		this.entrada1Desejada = entrada1Desejada;
	}

	private int getVez() {
		return vez;
	}

	private void setVez(int vez) {
		this.vez = vez;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	public void entre0() {
		setEntrada0Desejada(true);
		setVez(1);
		while (isEntrada1Desejada() && getVez() == 1) {
			Thread.yield();
		}
	}

	public void saia0() {
		setEntrada0Desejada(false);
	}

	public void entre1() {
		setEntrada1Desejada(true);
		setVez(0);
		while (isEntrada0Desejada() && getVez() == 0) {
			Thread.yield();
		}
	}

	public void saia1() {
		setEntrada1Desejada(false);
	}
}
