package org.seagulls.fatlife.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import java.util.Collections;
import java.util.List;
import org.seagulls.fatlife.R;
import org.seagulls.fatlife.database.OrderHelper;
import org.seagulls.fatlife.entity.Order;
import org.seagulls.fatlife.ui.order.OrderAdapter.OrderViewHolder;

public class OrderAdapter extends Adapter<OrderViewHolder> {

  private final List<Order> orders;

  public OrderAdapter(List<Order> orders) {
    Collections.reverse(orders);
    this.orders = orders;
  }

  @NonNull
  @Override
  public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
      int viewType) {
    View view = View.inflate(parent.getContext(), R.layout.item_order, null);
    return new OrderViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
    Order order = orders.get(position);
    holder.tvMakeTime.setText(order.getMakeTime());
    holder.tvWM.setText(order.getWashingMachine().getName());
    holder.tvWT.setText(order.getWashingType().getStr());
    holder.tvAmount.setText(order.getWashingType().getAmount());
    holder.tvTotalAmount.setText(order.getWashingType().getAmount());

    holder.tvDelete.setOnClickListener(v -> {
      OrderHelper helper = new OrderHelper(v.getContext());
      helper.delete(orders.get(position));
      helper.close();
      orders.remove(position);
      notifyItemRemoved(position);
    });
    holder.llDetail.setOnClickListener(v -> {
      Intent intent = new Intent(v.getContext(), OrderDetailsActivity.class);
      Bundle bundle = new Bundle();
      bundle.putSerializable("order", orders.get(position));
      intent.putExtras(bundle);
      v.getContext().startActivity(intent);
    });
  }

  @Override
  public int getItemCount() {
    return orders.size();
  }

  public static class OrderViewHolder extends ViewHolder {

    TextView tvMakeTime;

    TextView tvDelete;

    TextView tvWM;

    TextView tvWT;

    TextView tvAmount;

    TextView tvTotalAmount;

    LinearLayout llDetail;

    public OrderViewHolder(@NonNull View itemView) {
      super(itemView);
      tvMakeTime = itemView.findViewById(R.id.tv_make_time);
      tvDelete = itemView.findViewById(R.id.tv_delete);
      tvWM = itemView.findViewById(R.id.tv_wm);
      tvWT = itemView.findViewById(R.id.tv_wt);
      tvAmount = itemView.findViewById(R.id.tv_amount);
      tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
      llDetail = itemView.findViewById(R.id.ll_detail);
    }

  }

}
