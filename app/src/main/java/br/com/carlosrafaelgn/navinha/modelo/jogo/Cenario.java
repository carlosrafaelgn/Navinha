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
package br.com.carlosrafaelgn.navinha.modelo.jogo;

import br.com.carlosrafaelgn.navinha.modelo.desenho.Tela;
import br.com.carlosrafaelgn.navinha.modelo.elemento.ListaDeElementosDeTela;
import br.com.carlosrafaelgn.navinha.modelo.recurso.ArmazenamentoDeRecursos;
import br.com.carlosrafaelgn.navinha.modelo.recurso.Recurso;

public abstract class Cenario extends Recurso {
	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private ListaDeElementosDeTela listaDeElementosDeTela;
	private ArmazenamentoDeRecursos armazenamentoDeRecursosInicial;
	private boolean carregado;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	public Cenario() {
		setListaDeElementosDeTela(new ListaDeElementosDeTela());
		setArmazenamentoDeRecursosInicial(new ArmazenamentoDeRecursos());
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	protected final ListaDeElementosDeTela getListaDeElementosDeTela() {
		return listaDeElementosDeTela;
	}

	private void setListaDeElementosDeTela(ListaDeElementosDeTela listaDeElementosDeTela) {
		this.listaDeElementosDeTela = listaDeElementosDeTela;
	}

	private ArmazenamentoDeRecursos getArmazenamentoDeRecursosInicial() {
		return armazenamentoDeRecursosInicial;
	}

	private void setArmazenamentoDeRecursosInicial(ArmazenamentoDeRecursos armazenamentoDeRecursosInicial) {
		this.armazenamentoDeRecursosInicial = armazenamentoDeRecursosInicial;
	}

	public final boolean isCarregado() {
		return carregado;
	}

	private void setCarregado(boolean carregado) {
		this.carregado = carregado;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos privados e protegidos
	//----------------------------------------------------------------------------------------------

	protected abstract void inicieInternamente(ArmazenamentoDeRecursos armazenamentoDeRecursos);

	@Override
	protected void carregueInternamente() {
		getListaDeElementosDeTela().carregue();

		// Utilizamos a propriedade carregado para indicar o carregamento, já que o cenário não tem
		// um objeto em especial que possa ser utilizado para isso
		setCarregado(true);
	}

	@Override
	protected void libereInternamente() {
		// Vamos liberar toda a memória que não será mais utilizada

		getListaDeElementosDeTela().libere();

		// Utilizamos a propriedade carregado para indicar o carregamento, já que o cenário não tem
		// um objeto em especial que possa ser utilizado para isso
		setCarregado(false);
	}

	@Override
	protected void destruaInternamente() {
		// Vamos invalidar o objeto

		ListaDeElementosDeTela listaDeElementosDeTela = getListaDeElementosDeTela();
		if (listaDeElementosDeTela != null) {
			listaDeElementosDeTela.destrua();
			setListaDeElementosDeTela(null);
		}

		ArmazenamentoDeRecursos armazenamentoDeRecursosInicial = getArmazenamentoDeRecursosInicial();
		if (armazenamentoDeRecursosInicial != null) {
			armazenamentoDeRecursosInicial.destrua();
			setArmazenamentoDeRecursosInicial(null);
		}
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	public final void adicioneRecursoInicial(String nome, Recurso recurso) {
		getArmazenamentoDeRecursosInicial().adicione(nome, recurso);
	}

	public final void inicie() {
		ArmazenamentoDeRecursos armazenamentoDeRecursosInicial = getArmazenamentoDeRecursosInicial();
		if (armazenamentoDeRecursosInicial == null) {
			armazenamentoDeRecursosInicial = new ArmazenamentoDeRecursos();
		}

		inicieInternamente(armazenamentoDeRecursosInicial);

		// Vamos destruir tudo que sobrou dentro de armazenamentoDeRecursosInicial
		armazenamentoDeRecursosInicial.destrua();

		setArmazenamentoDeRecursosInicial(null);
	}

	public void interatividadeComJogadorPerdida() {
		// A implementação padrão simplesmente ignora o fato do jogo ter perdido a interatividade
		// com o jogador
	}

	public void interatividadeComJogadorRecuperada() {
		// A implementação padrão simplesmente ignora o fato do jogo ter recuperado a interatividade
		// com o jogador
	}

	public void botaoVoltarPressionado() {
		// A implementação padrão do botão voltar simplesmente encerra o jogo
		Jogo.getJogo().encerre();
	}

	public void processeEDesenheUmQuadro(float deltaSegundos) {
		// Preenche o fundo da tela com a cor atualmente configurada
		Tela.getTela().preencha();

		// Processa e desenha todos os elementos
		getListaDeElementosDeTela().processeEDesenheUmQuadro(deltaSegundos);
	}
}
