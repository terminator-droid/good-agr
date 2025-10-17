import React from "react";
import { Catalog } from "./components/Catalog";
import { Cart } from "./components/Cart";

export default function App() {
  const [page, setPage] = React.useState('catalog');
  return (
      <div style={{maxWidth: 800, margin: "0 auto", padding: "1em"}}>
        <h1>Агрегатор продуктов</h1>
        <button onClick={() => setPage('catalog')}>Каталог</button>
        <button onClick={() => setPage('cart')}>Корзина</button>
        <hr />
        {page === 'catalog' ? <Catalog /> : <Cart />}
      </div>
  );
}
