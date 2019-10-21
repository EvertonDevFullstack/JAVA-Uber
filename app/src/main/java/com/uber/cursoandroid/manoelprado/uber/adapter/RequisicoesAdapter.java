package com.uber.cursoandroid.manoelprado.uber.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.uber.cursoandroid.manoelprado.uber.R;
import com.uber.cursoandroid.manoelprado.uber.model.Requisicao;
import com.uber.cursoandroid.manoelprado.uber.model.Usuario;

import java.util.List;

public class RequisicoesAdapter extends RecyclerView.Adapter<RequisicoesAdapter.MyViewHolder> {

    private List<Requisicao> requisicoes;
    private Context context;
    private Usuario motorista;

    public RequisicoesAdapter(List<Requisicao> requisicoes, Context context, Usuario motorista) {
        this.requisicoes = requisicoes;
        this.context = context;
        this.motorista = motorista;
    }

    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_requisicoes, parent, false); //primeiro parametro: layout q vai ser utilizado (devemos criar o adapter_requisicoes)
        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Requisicao requisicao = requisicoes.get(position);
        Usuario passageiro  = requisicao.getPassageiro();

        //aqui a gente pode ate recuperar a lat e long do motorista, caso queira motorista.getla ......

        holder.nome.setText(passageiro.getNome());
        holder.distancia.setText("1 km - aproximadamente");

    }

    @Override
    public int getItemCount() {
        return requisicoes.size(); //retorna a quantidade de itens da requisicao
    }

    //Configuração do viewHolder
    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView nome, distancia;

        public MyViewHolder(View itemView){
            super (itemView);

            nome = itemView.findViewById(R.id.textRequisicaoNome);
            distancia = itemView.findViewById(R.id.textRequisicaoDistancia);
        }

    }

}
